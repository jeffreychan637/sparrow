package com.tmochida.sparrow.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tmochida.sparrow.R;

public class TweetCreateFragment extends Fragment {
    OnUserTweetListener mCallback;

    private Button mTweetBtn;
    private EditText mRecipientText;
    private EditText mContentText;

    public interface OnUserTweetListener {
        void sendTweet(String recipient, String content);
    }

    public void sendTweet(String recipient, String content) {
        mCallback.sendTweet(recipient, content);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnUserTweetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUserTweetListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.fragment_tweet_create, container, false);
        mRecipientText = (EditText) ll.findViewById(R.id.recipientText);
        mContentText = (EditText) ll.findViewById(R.id.contentText);
        return ll;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTweetBtn = (Button) view.findViewById(R.id.tweetBtn);
        mTweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTweet(mRecipientText.getText().toString(), mContentText.getText().toString());
            }
        });
    }
}
