package com.tmochida.sparrow.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tmochida.sparrow.R;
import com.tmochida.sparrow.adapters.KeyPairAdapter;
import com.tmochida.sparrow.tweet.KeyPairContainer;
import com.tmochida.sparrow.tweet.TweetStorage;

import java.util.ArrayList;

/**
 * Fragment to list Public keys stored on device.
 */
public class KeysFragment extends ListFragment {
    private ArrayList<KeyPairContainer> mKeys;
    private KeyPairAdapter mKeyPairAdapter;
    private TweetStorage mTweetStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // load public keys from device storage
        mTweetStorage = new TweetStorage();
        mKeys = mTweetStorage.loadKeyPairs(getActivity(), false);
        mKeyPairAdapter = new KeyPairAdapter(getActivity(), R.layout.item_keypair, mKeys);
        setListAdapter(mKeyPairAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_keys, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // save keys
        mTweetStorage.saveKeyPairs(getActivity(), mKeys, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
