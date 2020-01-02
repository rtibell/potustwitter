package com.tibell.potustwitter.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
@Configuration
//@PropertySource("classpath:twitter.properties")
@ConfigurationProperties(prefix = "twitter")
@Validated
public class SocialProperties {
    private static Logger logger = LoggerFactory.getLogger(SocialProperties.class);

    @Valid
    @NotNull
    private String consumerKey;

    @Valid
    @NotNull
    private String consumerSecret;

    @Valid
    @NotNull
    private String accessToken;

    @Valid
    @NotNull
    private String accessTokenSecret;

    @Valid
    @NotNull
    private String potusUser;

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getPotusUser() {
        return potusUser;
    }

    public void setPotusUser(String potusUser) {
        this.potusUser = potusUser;
    }

    @Override
    public String toString() {
        return "SocialProperties{" +
                "consumerKey='" + consumerKey + '\'' +
                ", consumerSecret='" + consumerSecret + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", accessTokenSecret='" + accessTokenSecret + '\'' +
                ", potusUser='" + potusUser + '\'' +
                '}';
    }
}
