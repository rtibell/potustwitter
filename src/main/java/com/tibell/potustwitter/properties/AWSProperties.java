package com.tibell.potustwitter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
@Configuration
//@PropertySource("classpath:aws.properties")
@ConfigurationProperties(prefix = "aws")
@Validated
public class AWSProperties {

    @Valid
    @NotNull
    private String s3BucketName;

    @Valid
    @NotNull
    private String s3MapName;

    @Valid
    @NotNull
    private String region;

    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getS3MapName() {
        return s3MapName;
    }

    public void setS3MapName(String s3MapName) {
        this.s3MapName = s3MapName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "AWSProperties{" +
                "s3BucketName='" + s3BucketName + '\'' +
                ", s3MapName='" + s3MapName + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
