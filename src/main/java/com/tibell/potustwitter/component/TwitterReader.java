package com.tibell.potustwitter.component;

import com.tibell.potustwitter.entity.Sentiment;
import com.tibell.potustwitter.properties.AWSProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.ComprehendClientBuilder;
import software.amazon.awssdk.services.comprehend.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import com.tibell.potustwitter.properties.SocialProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public class TwitterReader {
    private static Logger logger = LoggerFactory.getLogger(TwitterReader.class);
    private final S3Client s3;
    private final AWSProperties awsProp;

    private Twitter twitter;
    private Hashtable<Long, Boolean> tweetHT;
    private String user;
    private long prevSavedId = Long.MAX_VALUE;
    private int nrFiles;
    private String lastKey;
    private ComprehendClient comprehendClient;

    public TwitterReader(SocialProperties socProp, AWSProperties awsProp, String user) {
        this.awsProp = awsProp;
        this.user = user;
        this.tweetHT = new Hashtable<Long,Boolean>();
        logger.info("Twitter props -- " + socProp.toString());
        this.twitter = new TwitterTemplate(socProp.getConsumerKey(), socProp.getConsumerSecret(), socProp.getAccessToken(), socProp.getAccessTokenSecret());
        ComprehendClientBuilder comprehendClientBuilder = ComprehendClient.builder();
        comprehendClient = comprehendClientBuilder.build();
        Region region = Region.EU_CENTRAL_1;
        s3 = S3Client.builder().region(region).build();

        listTweetBucket();
    }

    public void getInitialList() {
        logger.info("at getInitialList(" + user + ")");
        procTweet(twitter.timelineOperations().getUserTimeline(user));
    }

    public void getNextList(int nrEntrys) {
        int entrys = Math.min(nrEntrys,200);
        logger.info("at getNextList(" + user + ") from " + prevSavedId);
        procTweet(twitter.timelineOperations().getUserTimeline(user, entrys, 1L, prevSavedId));
    }

    public void procTweet(List<Tweet> tweets) {
        tweets.forEach(tw -> {  saveTweetToS3(tw); setId(tw.getId());});
    }

    public void saveTweetToS3(Tweet tweet) {
        if (this.tweetHT.containsKey(tweet.getId()))  {
            return;
        }
        prevSavedId = Math.min(prevSavedId, tweet.getId());
        String shard = randomAlphaNumeric(6);
        String key = awsProp.getS3MapName() + "/" + shard + "_" + tweet.getIdStr() + ".txt";
        logger.info("Save tweet to S3 with key " + key);
        logger.info("Text: " + tweet.getText());
        logger.info("Unmodified text: " + tweet.getUnmodifiedText());
        logger.info("Retweet: " + tweet.isRetweet() + " From:" + tweet.getFromUser() + " Created: " + tweet.getCreatedAt());

        Sentiment sentiment = comprehendTweet(tweet, shard);
        ByteBuffer bb = ByteBuffer.wrap(sentiment.toJsonString().getBytes(Charset.defaultCharset()));
        logger.info("Final JSON: " + sentiment.toJsonString());

        // Put Object
        s3.putObject(PutObjectRequest.builder().bucket(awsProp.getS3BucketName()).key(key)
                        .build(),
                RequestBody.fromByteBuffer(bb));

    }

    public void listTweetBucket() {
        // List objects
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(awsProp.getS3BucketName()).prefix(awsProp.getS3MapName()).build();
        ListObjectsV2Response listObjectsV2Response;
        String nextToken;
        do {
            listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);
//            listObjectsResponse.contents().
//                    stream().filter(x -> x.key().startsWith(awsProp.getS3MapName())).
//                    forEach(x -> { setLatestIds(x.key());});
            List<S3Object> objectList = listObjectsV2Response.contents();
            for (S3Object s3Object : objectList) setLatestIds(s3Object.key());
            nextToken = listObjectsV2Response.nextContinuationToken();
            if (nextToken == null || nextToken.length() == 0) nextToken = lastKey;
            listObjectsV2Request = ListObjectsV2Request.builder().bucket(awsProp.getS3BucketName()).prefix(awsProp.getS3MapName()).continuationToken(nextToken).build();
        } while (listObjectsV2Response.isTruncated());
    }

    public void setLatestIds(String st) {
        this.nrFiles++;
        System.out.println(st + " files " + this.nrFiles);
        String patString = awsProp.getS3MapName() + "/([a-z]*)_([0-9]*)\\.txt";
        //System.out.println(patString);
        Pattern pat = Pattern.compile(patString);
        Matcher match = pat.matcher(st);
        if (match.matches()) {
            System.out.println(match.group(2));
            long id = Long.parseLong(match.group(2));
            setId(id);
            this.lastKey = match.group(2);
        } else {
            logger.info("Missmatch " + st);
        }
    }

    public Sentiment comprehendTweet(Tweet tweet, String key) {
        //ComprehendClientBuilder comprehendClientBuilder = ComprehendClient.builder();
        //ComprehendClient comprehendClient = comprehendClientBuilder.build();
        DetectSentimentRequest request = DetectSentimentRequest.builder().languageCode(LanguageCode.EN).text(tweet.getText()).build();
        DetectSentimentResponse response = comprehendClient.detectSentiment(request);
        SentimentType type = response.sentiment();
        logger.info("Sentiment type: " + type.toString());
        SentimentScore score = response.sentimentScore();
        logger.info("Sentiment score: " + score.toString());
        return new Sentiment(key, tweet.getId(),
                tweet.getText(), tweet.getUnmodifiedText(), tweet.getCreatedAt(),
                type.name(),
                score.positive(), score.negative(), score.neutral(), score.mixed());
    }

    private void setId(long id) {
        Long idObj = id;
        if (!this.tweetHT.containsKey(idObj)) this.tweetHT.put(idObj, Boolean.TRUE);
        else prevSavedId = Math.min(prevSavedId, id);
    }


    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz";
    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
