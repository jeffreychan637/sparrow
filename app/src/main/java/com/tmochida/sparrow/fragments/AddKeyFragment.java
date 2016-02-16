package com.tmochida.sparrow.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tmochida.sparrow.R;
import com.tmochida.sparrow.interfaces.OnPublicKeyAddListener;

/**
 * Fragment for adding an author's public key.
 */
public class AddKeyFragment extends Fragment {
    private OnPublicKeyAddListener mCallback;
    private Button mAddKeyBtn;
    private EditText mKeyAuthor;
    private EditText mKeyPublic;

    public void addPublicKey(String author, String key) {
        mCallback.addPublicKey(author, key);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPublicKeyAddListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPublicKeyAddListener");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.fragment_key_add, container, false);
        mKeyAuthor = (EditText) ll.findViewById(R.id.key_add_author);
        mKeyPublic = (EditText) ll.findViewById(R.id.key_add_public);
        return ll;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAddKeyBtn = (Button) view.findViewById(R.id.addKeyBtn);
        mAddKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PROTOBUFF", "addkey called!");
                addPublicKey(mKeyAuthor.getText().toString(), mKeyPublic.getText().toString());
            }
        });
    }
}
