package com.tmochida.sparrow.tweet;

import java.io.Serializable;
import java.security.KeyPair;

/**
 * Container class for key pairs tied to an author.
 */
public class KeyPairContainer implements Serializable {
    private String mAuthor;
    private KeyPair mKeyPair;

    public KeyPairContainer(String author, KeyPair keyPair) {
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
