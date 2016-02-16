package com.tmochida.sparrow.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tmochida.sparrow.R;
import com.tmochida.sparrow.adapters.KeyPairAdapter;
import com.tmochida.sparrow.tweet.KeyPairContainer;
import com.tmochida.sparrow.tweet.TweetStorage;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by takes on 2/16/2016.
 */
public class ViewSelfKeyFragment extends ListFragment {
    private ListView mListView;
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
        mListView = getListView();
        setupListViewListener();
    }

    private void setupListViewListener() {
        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item, int pos, long id) {
                        KeyPairContainer container = mKeys.get(pos);

                        String publicKeyString;
                        try {
                            PublicKey key = container.getKeyPair().getPublic();
                            publicKeyString = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
                        } catch (Exception e) {
                            publicKeyString = "Invalid key";
                        }

                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("PublicKey", publicKeyString);
                        clipboard.setPrimaryClip(clip);

                        Toast.makeText(getActivity(), publicKeyString,
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
        );
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // load public key of self from device storage
        mTweetStorage = new TweetStorage();
        mKeys = mTweetStorage.loadKeyPairs(getActivity(), true);
        mKeyPairAdapter = new KeyPairAdapter(getActivity(), R.layout.item_keypair, mKeys);
        setListAdapter(mKeyPairAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_key_self, container, false);
    }
}
