package com.tmochida.sparrow.tweet;

import java.io.Serializable;

import edu.berkeley.cs194.Tweet;

/**
 * Container class for Tweet objects.
 */
public class TweetContainer implements Serializable {
    private Tweet tweet;
    private long initTime;

    public TweetContainer(Tweet tweet) {
        super();
        this.tweet = tweet;
        this.initTime = System.currentTimeMillis();
    }

    public Tweet getTweet() {
        return this.tweet;
    }

    public long getInitTime() {
        return this.initTime;
    }
}
