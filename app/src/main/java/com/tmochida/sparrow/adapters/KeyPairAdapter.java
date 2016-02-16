package com.tmochida.sparrow.adapters;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tmochida.sparrow.R;
import com.tmochida.sparrow.tweet.KeyPairContainer;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by takes on 2/15/2016.
 */
public class KeyPairAdapter extends ArrayAdapter<KeyPairContainer> {
    public KeyPairAdapter(Context context, int viewResourceId, ArrayList<KeyPairContainer> keys) {
        super(context, viewResourceId, keys);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        KeyPairContainer container = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_keypair, parent, false);
        }
        // Lookup view for data population
        TextView author = (TextView) convertView.findViewById(R.id.keypair_author);
        TextView publicKey = (TextView) convertView.findViewById(R.id.keypair_public);
        author.setText(container.getAuthor());

        String publicKeyString;
        try {
            PublicKey key = container.getKeyPair().getPublic();
            publicKeyString = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
        } catch (Exception e) {
            publicKeyString = "Invalid key";
        }

        publicKey.setText(publicKeyString);
        // Return the completed view to render on screen
        return convertView;
    }
}
