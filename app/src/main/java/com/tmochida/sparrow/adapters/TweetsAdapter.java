package com.tmochida.sparrow.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jeffreychan637.sparrow.ExchangeState;
import com.tmochida.sparrow.Encryption;
import com.tmochida.sparrow.MainActivity;
import com.tmochida.sparrow.R;
import com.tmochida.sparrow.tweet.TweetContainer;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.berkeley.cs194.Tweet;

/**
 * TweetsAdapter
 */
public class TweetsAdapter extends ArrayAdapter<TweetContainer> {
    private Context mContext;

    public TweetsAdapter(Context context, int viewResourceId, ArrayList<TweetContainer> tweets) {
        super(context, viewResourceId, tweets);
        this.mContext = context;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TweetContainer container = getItem(position);
        Tweet tweet = container.getTweet();
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
        }
        // Lookup view for data population
        TextView tweetAuthor = (TextView) convertView.findViewById(R.id.tweetAuthor);
        TextView tweetConent = (TextView) convertView.findViewById(R.id.tweetContent);
        // Populate the data into the template view using the data object
        String author = tweet.author;
        if (tweet.recipient.length() > 0)
            author += " to " + tweet.recipient;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String deviceAuthor = preferences.getString("author_name_common", "");

        String newContent = tweet.content;
        if (tweet.encrypted) {
            if (deviceAuthor.equals(tweet.author)) {
                // Can't decrypt so we pull local stash of content
                newContent = container.getOriginalContent();
            } else {
                // RSA encrypted, need to decrypt to get AES key
                byte[] key_symmetric = Base64.decode(tweet.key_symmetric, Base64.DEFAULT);
                PrivateKey pkey = ((MainActivity) getContext()).getSelfPrivateKey();

                // Get AES key
                byte[] key_bytes = Encryption.decryptRSA(key_symmetric, pkey);
                SecretKey aes_key = new SecretKeySpec(key_bytes, 0, key_bytes.length, "AES");
                byte[] aes_key_bytes = aes_key.getEncoded();

                try {
                    // Decrypt content with AES key
                    byte[] content_bytes = Base64.decode(tweet.content, Base64.DEFAULT);
                    byte[] decodedData = Encryption.decrypt(aes_key_bytes, content_bytes);
                    newContent = new String(decodedData, "UTF-8");
                } catch (Exception e) {
                    Log.d("PROTOBUFF", "decode ERROR!");
                }
            }
        }

        // Return the completed view to render on screen
        tweetAuthor.setText(author);
        tweetConent.setText(newContent);
        return convertView;
    }
}
