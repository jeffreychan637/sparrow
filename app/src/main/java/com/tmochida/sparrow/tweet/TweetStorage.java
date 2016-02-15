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
    public static final String FILENAME_KEYS = "tweets.keys";

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

    public void saveKeys(Context context, ArrayList<TweetKeyPair> keys) {
        File file = new File(context.getFilesDir(), FILENAME_KEYS);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(keys);
            out.close();
        } catch (Exception e) {
        }
    }

    public ArrayList<TweetKeyPair> loadKeys(Context context) {
        ArrayList<TweetKeyPair> keys;

        File file = new File(context.getFilesDir(), FILENAME_TWEETS);
        try {
            FileInputStream stream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(stream);
            keys = (ArrayList<TweetKeyPair>) in.readObject();
            in.close();
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return keys;
    }
}