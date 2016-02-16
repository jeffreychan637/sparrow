package com.tmochida.sparrow;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.jeffreychan637.sparrow.BluetoothFragment;
import com.jeffreychan637.sparrow.DataSender;

import com.tmochida.sparrow.fragments.AddKeyFragment;
import com.tmochida.sparrow.fragments.KeysFragment;
import com.tmochida.sparrow.fragments.SettingsFragment;
import com.tmochida.sparrow.fragments.TweetCreateFragment;
import com.tmochida.sparrow.fragments.TweetListFragment;
import com.tmochida.sparrow.fragments.ViewSelfKeyFragment;
import com.tmochida.sparrow.interfaces.OnChangeSettingsListener;
import com.tmochida.sparrow.interfaces.OnPublicKeyAddListener;
import com.tmochida.sparrow.interfaces.OnUserTweetListener;
import com.tmochida.sparrow.tweet.KeyPairContainer;
import com.tmochida.sparrow.tweet.TweetContainer;
import com.tmochida.sparrow.tweet.TweetStorage;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.cs194.Feature;
import edu.berkeley.cs194.Handshake;
import edu.berkeley.cs194.Tweet;
import edu.berkeley.cs194.TweetExchange;

public class MainActivity extends Activity implements OnUserTweetListener, OnPublicKeyAddListener,
        OnChangeSettingsListener, DataSender {
    private static final String NAME_BLUETOOTH = "bluetoothFragment";
    private static final String NAME_BLE = "bleFragment";
    
    private TweetListFragment mFeedFragment;
    private Handshake mReceivedHandshake;

    @Override
    public void changeBleMode(boolean use_ble) {
        ;
    }

    @Override
    public void addPublicKey(String author, String key) {
        FragmentManager fm = getFragmentManager();
        fm.popBackStack("KeysFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (author.length() > 0 && key.length() > 0) {
            TweetStorage storage = new TweetStorage();
            ArrayList<KeyPairContainer> keys = storage.loadKeyPairs(this, false);
            int delete = -1;
            for (int i = 0; i < keys.size(); i++) {
                KeyPairContainer container = keys.get(i);
                if (container.getAuthor().equals(author)) {
                    delete = i;
                    break;
                }
            }
            if (delete > -1)
                keys.remove(delete);
            KeyPairContainer newContainer = createKeyPairContainer(author, key);
            if (newContainer != null)
                keys.add(newContainer);
            storage.saveKeyPairs(this, keys, false);
        }

        fm = getFragmentManager();
        fm.popBackStack();
    }

    public PublicKey getPublicKey(String author) {
        TweetStorage storage = new TweetStorage();
        ArrayList<KeyPairContainer> keys = storage.loadKeyPairs(this, false);
        for (KeyPairContainer container : keys) {
            if (container.getAuthor().equals(author))
                return container.getKeyPair().getPublic();
        }
        return null;
    }

    public PrivateKey getSelfPrivateKey() {
        TweetStorage storage = new TweetStorage();
        ArrayList<KeyPairContainer> keys = storage.loadKeyPairs(this, true);
        if (keys.size() > 0)
            return keys.get(0).getKeyPair().getPrivate();
        return null;
    }

    private KeyPairContainer createKeyPairContainer(String author, String key) {
        KeyPairContainer container;
        try {
            byte[] keyInBytes = Base64.decode(key, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyInBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publickKey = keyFactory.generatePublic(keySpec);
            KeyPair pair = new KeyPair(publickKey, null);
            container = new KeyPairContainer(author, pair);
        } catch (Exception e) {
            return null;
        }
        return container;
    }

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
            transaction.addToBackStack(NAME_BLUETOOTH);
            transaction.commit();

            // Fragment for Tweet feed
            mFeedFragment = new TweetListFragment();
            transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.frame_container, mFeedFragment);
            transaction.commit();
        }

        // generate private keypair if not already created (running for first time)
        TweetStorage tweetStorage = new TweetStorage();
        ArrayList<KeyPairContainer> keys = tweetStorage.loadKeyPairs(this, true);
        if (keys.size() != 1) {
            keys = new ArrayList<>();
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                KeyPair keyPair = keyGen.genKeyPair();

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String author = sharedPref.getString("author_name_common", "anonymous");
                keys.add(new KeyPairContainer(author, keyPair));
            } catch (NoSuchAlgorithmException e) {
                Log.d("PROTOBUFF", "algorithm not supported");
            }

            // save generated KeyPair into storage
            tweetStorage.saveKeyPairs(this, keys, true);
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

        FragmentManager fm = getFragmentManager();
        Fragment hideFragment = fm.findFragmentById(R.id.frame_container);
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(hideFragment);

        if (id == R.id.action_keys_view) {
            ft.add(R.id.frame_container, new KeysFragment());
            ft.addToBackStack("KeysFragment");
            ft.commit();
            return true;
        } else if (id == R.id.action_keys_add) {
            ft.add(R.id.frame_container, new AddKeyFragment());
            ft.addToBackStack("AddKeyFragment");
            ft.commit();
            return true;
        } else if (id == R.id.action_keys_view_self) {
            ft.add(R.id.frame_container, new ViewSelfKeyFragment());
            ft.addToBackStack("ViewSelfKeyFragment");
            ft.commit();
            return true;
        } else if (id == R.id.action_settings) {
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
        return payload;
    };

    public void processReceivedHandshake(byte[] data) {
        try {
            mReceivedHandshake = Handshake.ADAPTER.decode(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String author = sharedPref.getString("author_name_common", "anonymous");

        for (Tweet t : exchange.tweets) {
            boolean exists = false;
            for (TweetContainer tc : globalTweets) {
                Tweet tweetInList = tc.getTweet();
                if (tweetInList.id.equals(t.id) && tweetInList.author.equals(t.author)) {
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
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFeedFragment.getAdapter().notifyDataSetChanged();
            }
        });
    }
}
