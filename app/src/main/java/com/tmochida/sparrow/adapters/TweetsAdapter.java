package com.tmochida.sparrow.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tmochida.sparrow.Encryption;
import com.tmochida.sparrow.MainActivity;
import com.tmochida.sparrow.R;
import com.tmochida.sparrow.tweet.TweetContainer;

import java.security.PrivateKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.berkeley.cs194.Tweet;

/**
 * TweetsAdapter
 */
public class TweetsAdapter extends ArrayAdapter<TweetContainer> {
    private static final int COLOR_ALERT = Color.RED;
    private static final int COLOR_WARN = Color.YELLOW;
    private static final int COLOR_INSECURE = Color.GRAY;
    private static final int COLOR_VERIFIED = Color.GREEN;

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
                // DM sent from device. Can't decrypt so we pull local stash of content
                newContent = container.getOriginalContent();
                convertView.setBackgroundColor(COLOR_VERIFIED);
            } else {
                convertView.setBackgroundColor(COLOR_VERIFIED);

                // RSA encrypted, need to decrypt to get AES key
                byte[] key_symmetric = Base64.decode(tweet.key_symmetric, Base64.DEFAULT);
                PrivateKey pkey = ((MainActivity) getContext()).getSelfPrivateKey();
                if (pkey == null) {
                    convertView.setBackgroundColor(Color.YELLOW);
                }

                // Get AES key
                SecretKey aes_key = null;
                byte[] aes_key_bytes = null;
                byte[] key_bytes = Encryption.decryptRSA(key_symmetric, pkey);
                if (key_bytes != null) {
                    aes_key = new SecretKeySpec(key_bytes, 0, key_bytes.length, "AES");
                    aes_key_bytes = aes_key.getEncoded();
                }

                try {
                    // Decrypt content with AES key
                    byte[] content_bytes = Base64.decode(tweet.content, Base64.DEFAULT);
                    byte[] decodedData = Encryption.decrypt(aes_key_bytes, content_bytes);
                    newContent = new String(decodedData, "UTF-8");
                } catch (Exception e) {
                    // Error i.e. wrong key, alert user
                    convertView.setBackgroundColor(COLOR_ALERT);
                }
            }
        }
done:
        // Return the completed view to render on screen
        tweetAuthor.setText(author);
        tweetConent.setText(newContent);
        return convertView;
    }
}
