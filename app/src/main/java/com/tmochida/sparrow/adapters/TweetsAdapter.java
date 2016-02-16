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

import com.tmochida.sparrow.Encryption;
import com.tmochida.sparrow.MainActivity;
import com.tmochida.sparrow.R;
import com.tmochida.sparrow.tweet.TweetContainer;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

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


        String content = null;
        byte[] sharedSecret = null;
        if (tweet.encrypted) {
            if (deviceAuthor.equals(tweet.author)) {
                content = container.getOriginalContent();
                /*Log.d("PROTOBUFF", "device author equals author, getting from container");

                // get sharedsecret from container
                sharedSecret = container.getSharedSecret();
                Log.d("PROTOBUFF", "secret length " + sharedSecret.length);*/
            } else {
                // need to decrypt with private key
                /*PrivateKey privateKey = ((MainActivity) getContext()).getSelfPrivateKey();
                if (privateKey != null) {
                    String symmetric = tweet.key_symmetric;



                    Log.d("PROTOBUFF", "got shared key symmetric.");
                    sharedSecret = Encryption.decryptRSA(Base64.decode(symmetric, Base64.DEFAULT), privateKey);
                }*/

                String symmetric = tweet.key_symmetric;
                byte[] key_aes = Base64.decode(symmetric, Base64.DEFAULT);
                try {
                    content = new String(Encryption.decrypt(key_aes, Base64.decode(tweet.content, Base64.DEFAULT)));
                } catch (Exception e) {
                    Log.d("PROTOBUFF", "error! " + key_aes.length);
                    e.printStackTrace();
                }
            }
        }
        //Log.d("PROTOBUFF", "shared Secret: " + sharedSecret.length());
        //Log.d("PROTOBUFF", ""+sharedSecret.getBytes().length);
        /*if (sharedSecret != null) {
            Log.d("PROTOBUFF", "decrypting length: " + sharedSecret.length);

            // decrypt content
            try {
                content = new String(Encryption.decrypt(sharedSecret, tweet.content.getBytes()));
            } catch (Exception e) {
                Log.d("PROTOBUFF", "error! " + sharedSecret.length);
                e.printStackTrace();
            }
        }*/

        Log.d("PROTOBUFF", "recipient " + tweet.recipient);
        Log.d("PROTOBUFF", "content is : " + content);
        Log.d("PROTOBUFF", "original content is : " + tweet.content + "\n");

        tweetAuthor.setText(author);
        tweetConent.setText(content);

        // Return the completed view to render on screen
        return convertView;
    }
}