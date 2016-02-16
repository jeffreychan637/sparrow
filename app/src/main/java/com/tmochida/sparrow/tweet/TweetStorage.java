package com.tmochida.sparrow.tweet;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Class for storing/loading data into storage.
 */
public class TweetStorage {
    public static final String FILENAME_TWEETS = "tweets.data";
    public static final String FILENAME_KEYPAIR_SELF = "keys.self";
    public static final String FILENAME_KEYAPIR_ELSE = "keys.else";

    public void saveTweets(Context context, ArrayList<TweetContainer> tweets) {
        File file = new File(context.getFilesDir(), FILENAME_TWEETS);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(tweets);
            out.close();
        } catch (Exception e) {
        }
    }

    public ArrayList<TweetContainer> loadTweets(Context context) {
        ArrayList<TweetContainer> tweets;

        File file = new File(context.getFilesDir(), FILENAME_TWEETS);
        try {
            FileInputStream stream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(stream);
            tweets = (ArrayList<TweetContainer>) in.readObject();
            in.close();
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return tweets;
    }

    public void saveKeyPairs(Context context, ArrayList<KeyPairContainer> keys, boolean self) {
        File file = new File(context.getFilesDir(), getKeyPairFileName(self));
        try {
            FileOutputStream stream = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(keys);
            out.close();
        } catch (Exception e) {
        }
    }

    public ArrayList<KeyPairContainer> loadKeyPairs(Context context, boolean self) {
        ArrayList<KeyPairContainer> keys;

        File file = new File(context.getFilesDir(), getKeyPairFileName(self));
        try {
            FileInputStream stream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(stream);
            keys = (ArrayList<KeyPairContainer>) in.readObject();
            in.close();
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return keys;
    }

    private String getKeyPairFileName(boolean self) {
        if (self)
            return FILENAME_KEYPAIR_SELF;
        else
            return FILENAME_KEYAPIR_ELSE;
    }
}
