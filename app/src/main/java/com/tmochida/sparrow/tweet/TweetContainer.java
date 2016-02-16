package com.tmochida.sparrow.tweet;

import java.io.Serializable;

import edu.berkeley.cs194.Tweet;

/**
 * Container class for Tweet objects.
 */
public class TweetContainer implements Serializable {
    private Tweet tweet;
    private long initTime;
    private byte[] sharedSecret;
    private String originalContent;

    public TweetContainer(Tweet tweet) {
        this.tweet = tweet;
        this.initTime = System.currentTimeMillis();
        this.sharedSecret = null;
    }

    public TweetContainer(Tweet tweet, String originalContent) {
        this.tweet = tweet;
        this.initTime = System.currentTimeMillis();
        this.originalContent = originalContent;
    }

    public Tweet getTweet() {
        return this.tweet;
    }

    public long getInitTime() {
        return this.initTime;
    }

    public byte[] getSharedSecret() {
        return this.sharedSecret;
    }

    public String getOriginalContent() {
        return this.originalContent;
    }
}
