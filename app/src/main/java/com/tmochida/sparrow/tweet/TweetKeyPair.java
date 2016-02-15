package com.tmochida.sparrow.tweet;

import java.io.Serializable;
import java.security.KeyPair;

/**
 * Container class for holding crypto key pair for an author.
 */
public class TweetKeyPair  implements Serializable {
    private String mAuthor;
    private KeyPair mKeyPair;

    public TweetKeyPair(String author, KeyPair keyPair) {
        mAuthor = author;
        mKeyPair = keyPair;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public KeyPair getKeyPair() {
        return mKeyPair;
    }
}