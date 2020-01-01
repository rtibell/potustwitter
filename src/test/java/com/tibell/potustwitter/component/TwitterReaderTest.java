package com.tibell.potustwitter.component;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitterReaderTest {
    private static Logger logger = LoggerFactory.getLogger(TwitterReaderTest.class);
    @Test
    public void randomAlphaNumeric1() {
        Assert.assertEquals(3, genRandomAlphaNumeric(3).length());
        Assert.assertEquals(4, genRandomAlphaNumeric(4).length());
        Assert.assertEquals(5, genRandomAlphaNumeric(5).length());
        Assert.assertEquals(6, genRandomAlphaNumeric(6).length());
    }

    public String genRandomAlphaNumeric(int size) {
        String rndString = TwitterReader.randomAlphaNumeric(size);
        logger.info(rndString);
        return rndString;
    }
}