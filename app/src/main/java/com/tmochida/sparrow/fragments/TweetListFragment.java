package com.tmochida.sparrow.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.ListFragment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tmochida.sparrow.Encryption;
import com.tmochida.sparrow.MainActivity;
import com.tmochida.sparrow.R;
import com.tmochida.sparrow.tweet.KeyPairContainer;
import com.tmochida.sparrow.tweet.TweetContainer;
import com.tmochida.sparrow.tweet.TweetStorage;
import com.tmochida.sparrow.adapters.TweetsAdapter;

import java.security.Key;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.berkeley.cs194.Tweet;

/**
 * A fragment for feed: display tweets device remembers
 */
public class TweetListFragment extends ListFragment {
    private static final String KEY_AUTHOR_NAME = "author_name_common";
    private static final String KEY_TWEET_ID = "tweet_id";
    private static final String KEY_USE_ENCRYPT = "encryption_use";

    private static final String DEF_AUTHOR_NAME = "anonymous";
    private static final String DEF_TWEET_ID = "0";

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    private TweetsAdapter mTweetsAdapter;
    private TweetStorage mTweetStorage;
    private ArrayList<TweetContainer> mGlobalTweets;
    private ArrayList<TweetContainer> mViewableTweets;
    private ListView mListView;

    public void updateTweet(String recipient, String content) {
        if (content.length() < 1)
            return;

        int id;
        String author, keySharedSecret, signature;

        author = mSharedPrefs.getString(KEY_AUTHOR_NAME, DEF_AUTHOR_NAME);
        try {
            id = Integer.parseInt(mSharedPrefs.getString(KEY_TWEET_ID, DEF_TWEET_ID));
        } catch (Exception e) {
            id = 0;
        }

        boolean encrypt = mSharedPrefs.getBoolean(KEY_USE_ENCRYPT, false);
        boolean encryptedData = false;


        keySharedSecret = "";
        signature = "";

        byte[] key = null;
        String newContent = content;
        if (encrypt && recipient.length() > 0) {
            // Get public key of recipient
            PublicKey pkey = ((MainActivity)getActivity()).getPublicKey(recipient);
            if (pkey != null) {
                // can send encrypted message
                try {
                    // get secret key for AES
                    byte[] iv = "MUCH WOW".getBytes("UTF-8");
                    byte[] data = content.getBytes("UTF-8");
                    KeyGenerator kGen = KeyGenerator.getInstance("AES");
                    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                    sr.setSeed(iv);
                    kGen.init(128, sr);
                    SecretKey skey = kGen.generateKey();
                    key = skey.getEncoded();

                    byte[] encryptedContent = Encryption.encrypt(data);
                    newContent = Base64.encodeToString(encryptedContent, Base64.DEFAULT);

                    byte[] decoded = Base64.decode(newContent, Base64.DEFAULT);
                    byte[] decodedData = Encryption.decrypt(key, decoded);

                    Log.d("PROTOBUFF", "content: " + content);
                    Log.d("PROTOBUFF", "encoded content: " + newContent);
                    Log.d("PROTOBUFF", "decoded: " + new String(decodedData, "UTF-8"));

                    encryptedData = true;

                    keySharedSecret = Base64.encodeToString(key, Base64.DEFAULT);

                    // encrypt AES key using RSA
                    byte[] encryptedSecret = Encryption.encryptRSA(key, pkey);
                    keySharedSecret = Base64.encodeToString(encryptedSecret, Base64.DEFAULT);

                } catch (Exception e) {
                    Log.e("PROTOBUFF", "Exception caught");
                }
            }
        }

        Tweet newTweet = new Tweet(id, author, newContent, recipient, encryptedData, keySharedSecret, signature);
        TweetContainer newTweetContainer;
        if (encryptedData && key != null) {
            newTweetContainer = new TweetContainer(newTweet, content);
            Log.d("PROTOBUFF", "added with aes key. " + key.length);
        } else {
            newTweetContainer = new TweetContainer(newTweet);
            Log.d("PROTOBUFF", "added with no key. ");
        }

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = getListView();
        setupListViewListener();
    }

    private void setupListViewListener() {
        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item, int pos, long id) {
                        TweetContainer container = mViewableTweets.get(pos);

                        String content= container.getTweet().content;

                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Content", content);
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(getActivity(), content,
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
        );
    }
}
