package com.tmochida.sparrow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tmochida.sparrow.R;
import com.tmochida.sparrow.tweet.TweetContainer;

import java.util.ArrayList;

import edu.berkeley.cs194.Tweet;

/**
 * TweetsAdapter
 */
public class TweetsAdapter extends ArrayAdapter<TweetContainer> {
    public TweetsAdapter(Context context, int viewResourceId, ArrayList<TweetContainer> tweets) {
        super(context, viewResourceId, tweets);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Tweet tweet = getItem(position).getTweet();
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
        tweetAuthor.setText(author);
        tweetConent.setText(tweet.content);
        // Return the completed view to render on screen
        return convertView;
    }
}