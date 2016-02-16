package com.tmochida.sparrow.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.ListFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tmochida.sparrow.R;
import com.tmochida.sparrow.tweet.TweetContainer;
import com.tmochida.sparrow.tweet.TweetStorage;
import com.tmochida.sparrow.adapter.TweetsAdapter;

import java.util.ArrayList;

import edu.berkeley.cs194.Tweet;

/**
 * A fragment for feed: display tweets device remembers
 */
public class TweetListFragment extends ListFragment {
    private static final String KEY_AUTHOR_NAME = "author_name_common";
    private static final String KEY_TWEET_ID = "tweet_id";
    private static final String DEF_AUTHOR_NAME = "anonymous";
    private static final String DEF_TWEET_ID = "0";

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    private TweetsAdapter mTweetsAdapter;
    private TweetStorage mTweetStorage;
    private ArrayList<TweetContainer> mGlobalTweets;
    private ArrayList<TweetContainer> mViewableTweets;

    public void updateTweet(String recipient, String content) {
        if (content.length() < 1)
            return;

        String author = mSharedPrefs.getString(KEY_AUTHOR_NAME, DEF_AUTHOR_NAME);
        int id;
        try {
            id = Integer.parseInt(mSharedPrefs.getString(KEY_TWEET_ID, DEF_TWEET_ID));
        } catch (Exception e) {
            id = 0;
        }

        Tweet newTweet = new Tweet(id, author, content, recipient);
        TweetContainer newTweetContainer = new TweetContainer(newTweet);

        mGlobalTweets.add(newTweetContainer);
        mViewableTweets.add(newTweetContainer);

        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(KEY_TWEET_ID, Integer.toString(id + 1));
        editor.apply();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // load tweets, id from storage
        mTweetStorage = new TweetStorage();
        mGlobalTweets = mTweetStorage.loadTweets(getActivity());

        String author = mSharedPrefs.getString(KEY_AUTHOR_NAME, DEF_AUTHOR_NAME);
        mViewableTweets = new ArrayList<>();
        for (TweetContainer tc : mGlobalTweets) {
            Tweet t = tc.getTweet();
            // Don't display irrelevant private messages
            if (t.recipient.length() > 0 && !(t.recipient.equals(author) || t.author.equals(author)))
                continue;
            mViewableTweets.add(tc);
        }

        mTweetsAdapter = new TweetsAdapter(getActivity(), R.layout.item_tweet, mViewableTweets);
        setListAdapter(mTweetsAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTweetStorage.saveTweets(getActivity(), mGlobalTweets);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public ArrayList<TweetContainer> getGlobalTweets() {
        return mGlobalTweets;
    }

    public ArrayList<TweetContainer> getViewableTweets() {
        return mViewableTweets;
    }

    public TweetsAdapter getAdapter() {
        return mTweetsAdapter;
    }
}
