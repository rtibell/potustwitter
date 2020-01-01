package com.tibell.potustwitter.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import software.amazon.awssdk.core.util.json.JacksonUtils;

import java.util.Date;

public class Sentiment {
    private String shard;
    private long tweetId;
    private String text;
    private String rawText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Date created;
    private String sentiment;
    private float positive;
    private float negative;
    private float neutral;
    private float mixed;

    public Sentiment(String shard, long tweetId, String text, String rawText, Date created, String sentiment, float positive, float negative, float neutral, float mixed) {
        this.shard = shard;
        this.tweetId = tweetId;
        this.text = text;
        this.rawText = rawText;
        this.created = created;
        this.sentiment = sentiment;
        this.positive = positive;
        this.negative = negative;
        this.neutral = neutral;
        this.mixed = mixed;
    }

    public String getShard() {
        return shard;
    }

    public long getTweetId() {
        return tweetId;
    }

    public String getText() {
        return text;
    }

    public String getRawText() {
        return rawText;
    }

    public Date getCreated() {
        return created;
    }

    public String getSentiment() {
        return sentiment;
    }

    public float getPositive() {
        return positive;
    }

    public float getNegative() {
        return negative;
    }

    public float getNeutral() {
        return neutral;
    }

    public float getMixed() {
        return mixed;
    }

    @Override
    public String toString() {
        return "Sentiment{" +
                "shard='" + shard + '\'' +
                ", tweetId=" + tweetId +
                ", text='" + text + '\'' +
                ", rawText='" + rawText + '\'' +
                ", created=" + created +
                ", sentiment='" + sentiment + '\'' +
                ", positive=" + positive +
                ", negative=" + negative +
                ", neutral=" + neutral +
                ", mixed=" + mixed +
                '}';
    }

    public String toJsonString() {
        return JacksonUtils.toJsonString(this);
    }

    public String toJsonPrettyString() {
        return JacksonUtils.toJsonPrettyString(this);
    }
}
