package com.tibell.potustwitter.application;

import com.tibell.potustwitter.component.TwitterReader;
import com.tibell.potustwitter.config.SocialConfig;
import com.tibell.potustwitter.properties.AWSProperties;
import com.tibell.potustwitter.properties.SocialProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@SuppressWarnings("ALL")
@SpringBootApplication
@ComponentScan({"com.tibell.potustwitter.application", "com.tibell.potustwitter.config", "com.tibell.potustwitter.properties"})
@ConfigurationPropertiesScan({"com.tibell.potustwitter.properties"})
public class PotusTwitterApplication {
    private static Logger logger = LoggerFactory.getLogger(PotusTwitterApplication.class);

    @Autowired
    private SocialProperties socProp;

    @Autowired
    private AWSProperties awsProp;

    @Autowired
    private SocialConfig socialconfig;


    public static void main(String[] args) throws Exception {
        logger.info("Starting PotusTwitterApplication");
        SpringApplication.run(PotusTwitterApplication.class, args);
    }

    @PostConstruct
    private void init() {
        logger.info("Setting region to " + awsProp.getRegion());
        System.setProperty("AWS_REGION", awsProp.getRegion());

        logger.info("Spring Boot - SocialProperties");
        logger.info(socProp.toString());

        logger.info("Connection registration");
        socialconfig.connectionFactoryLocator();

        TwitterReader twr = new TwitterReader(socProp, awsProp, "realDonaldTrump");
        logger.info("Get initial");
        twr.getInitialList();

        for (int i = 0; i < 16; i++) {
            logger.info("Get 200 at " + i);
            twr.getNextList(200);
        }

    }
}
