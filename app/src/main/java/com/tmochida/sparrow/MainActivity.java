package com.tmochida.sparrow;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.jeffreychan637.sparrow.BluetoothFragment;
import com.tmochida.sparrow.fragment.SettingsFragment;
import com.tmochida.sparrow.fragment.TweetCreateFragment;
import com.tmochida.sparrow.fragment.TweetListFragment;
import com.tmochida.sparrow.tweet.TweetContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.cs194.Feature;
import edu.berkeley.cs194.Handshake;
import edu.berkeley.cs194.Tweet;
import edu.berkeley.cs194.TweetExchange;

public class MainActivity extends Activity implements TweetCreateFragment.OnUserTweetListener, BluetoothFragment.DataSender {
    private TweetListFragment mFeedFragment;
    private Handshake mReceivedHandshake;

    @Override
    public void sendTweet(String recipient, String content) {
        TweetListFragment fragment = mFeedFragment;
        fragment.updateTweet(recipient, content);

        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.hide(mFeedFragment);
                ft.add(R.id.frame_container, new TweetCreateFragment());
                ft.addToBackStack("tweetfrag");
                ft.commit();
            }
        });

        // set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {
            FragmentTransaction transaction;

            // Start fragment for Bluetooth
            BluetoothFragment BT = new BluetoothFragment();
            transaction = getFragmentManager().beginTransaction();
            transaction.add(BT, "bluetoothFragment");
            transaction.commit();

            // Fragment for Tweet feed
            mFeedFragment = new TweetListFragment();
            transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.frame_container, mFeedFragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_bluetooth) {
            return true;
        }
        if (id == R.id.action_settings) {
            FragmentManager fm = getFragmentManager();
            Fragment hideFragment = fm.findFragmentById(R.id.frame_container);

            FragmentTransaction ft = fm.beginTransaction();
            ft.hide(hideFragment);
            ft.add(R.id.frame_container, new SettingsFragment());
            ft.addToBackStack(null);
            ft.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public byte[] sendHandshakeOut() {
        List<Feature> features = new ArrayList<>();
        features.add(Feature.BASIC);
        byte[] payload = (new Handshake(features)).encode();
        Log.d("PROTOBUFF_HANDSHAKE", "Sending handshake, size: " + payload.length);
        return payload;
    };

    public void processReceivedHandshake(byte[] data) {
        try {
            mReceivedHandshake = Handshake.ADAPTER.decode(data);
        } catch (IOException e) {
            Log.d("PROTOBUFF_HANDSHAKE", "Caught IOException while receiving HandShake");
            e.printStackTrace();
        }
        Log.d("PROTOBUFF_HANDSHAKE", "Received handshake " + mReceivedHandshake.toString());
    }

    /* Returns a TweetExchange as byte[] encoded as payload for protocol buffer. */
    public byte[] sendDataOut() {
        List<TweetContainer> globalTweets = mFeedFragment.getGlobalTweets();
        List<Tweet> tweets = new ArrayList<>(globalTweets.size());
        for (TweetContainer tc : globalTweets) {
            tweets.add(tc.getTweet());
        }

        TweetExchange tweetExchange = new TweetExchange(tweets);
        return tweetExchange.encode();
    };

    /* Decodes passed in byte[] into a TweetExchange, adds new messages in local tweet list. */
    public void processReceivedData(byte[] data) {
        ArrayList<TweetContainer> globalTweets = mFeedFragment.getGlobalTweets();
        ArrayList<TweetContainer> viewableTweets = mFeedFragment.getViewableTweets();
        TweetExchange exchange;
        try {
            exchange = TweetExchange.ADAPTER.decode(data);
        } catch (IOException e) {
            Log.d("PROTOBUFF_TWEETEXCHANGE", "Caught IOException while receiving TweetExchange");
            e.printStackTrace();
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String author = sharedPref.getString("author_name_common", "anonymous");

        for (Tweet t : exchange.tweets) {
            boolean exists = false;
            for (TweetContainer tc : globalTweets) {
                Tweet tweetInList = tc.getTweet();
                if (tweetInList.id == t.id && tweetInList.author.equals(t.author)) {
                    exists = true;
                    break;
                }
            }
            if (exists)
                continue; // Tweet already added before, don't add duplicate
            TweetContainer tc = new TweetContainer(t);
            globalTweets.add(tc);
            if (t.recipient.length() == 0 || t.recipient.equals(author))
                viewableTweets.add(tc);
        }
        mFeedFragment.refreshView();
        Log.d("PROTOBUFF_TWEETEXCHANGE", "Received TweetExchange. Number of tweets: " + exchange.tweets.size());
    }
}
