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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class TwitterReader {
    private static Logger logger = LoggerFactory.getLogger(TwitterReader.class);
    private final S3Client s3;
    private final SocialProperties socProp;
    private final AWSProperties awsProp;
    private final Region region = Region.EU_CENTRAL_1;

    private Twitter twitter;

    String user;
    long maxId;
    long minId;

    public TwitterReader(SocialProperties socProp, AWSProperties awsProp, String user) {
        this.socProp = socProp;
        this.awsProp = awsProp;
        this.user = user;
        this.maxId = 1;
        this.minId = Long.MAX_VALUE;
        logger.info("Twitter props -- " + socProp.toString());
        this.twitter = new TwitterTemplate(socProp.getConsumerKey(), socProp.getConsumerSecret(), socProp.getAccessToken(), socProp.getAccessTokenSecret());
        s3 = S3Client.builder().region(region).build();
    }

    public void getInitialList() {
        logger.info("at getInitialList(" + user + ")");
        procTweet(twitter.timelineOperations().getUserTimeline(user));
    }

    public void getNextList(int nrEntrys) {
        int entrys = Math.min(nrEntrys,200);
        logger.info("at getNextList(" + user + ") from " + minId);
        procTweet(twitter.timelineOperations().getUserTimeline(user, entrys, 1l, minId));
    }

    public void procTweet(List<Tweet> tweets) {
        tweets.forEach(tw -> {  saveTweetToS3(tw); setId(tw.getId());});
    }

    public void saveTweetToS3(Tweet tweet) {
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

    public Sentiment comprehendTweet(Tweet tweet, String key) {
        ComprehendClientBuilder bulder = ComprehendClient.builder();
        ComprehendClient cc = bulder.build();
        DetectSentimentRequest request = DetectSentimentRequest.builder().languageCode(LanguageCode.EN).text(tweet.getText()).build();
        DetectSentimentResponse response = cc.detectSentiment(request);
        SentimentType type = response.sentiment();
        logger.info("Sentiment type: " + type.toString());
        SentimentScore score = response.sentimentScore();
        logger.info("Sentiment score: " + score.toString());
        Sentiment sentiment = new Sentiment(key, tweet.getId(),
                tweet.getText(), tweet.getUnmodifiedText(), tweet.getCreatedAt(),
                type.name(),
                score.positive(), score.negative(), score.neutral(), score.mixed());
        return sentiment;
    }

    private void setId(long id) {
        minId = Math.min(maxId, id);
        maxId = Math.max(maxId, id);
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
