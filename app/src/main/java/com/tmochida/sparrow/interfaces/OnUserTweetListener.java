package com.tmochida.sparrow.interfaces;

public interface OnUserTweetListener {
    void sendTweet(String recipient, String content);
}