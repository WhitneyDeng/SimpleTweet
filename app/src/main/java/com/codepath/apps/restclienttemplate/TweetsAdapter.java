package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.util.List;

// signature: extends: an adapter for recyclerview that holds tweets (viewholder)
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>
{
    Context context;
    List<Tweet> tweets;

    // pass in context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets)
    {
        this.context = context;
        this.tweets = tweets;
    }

    // for each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // populate values based on position of item/row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        // get data of item at position
        Tweet tweet = tweets.get(position);

        // bind tweet data to view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount()
    {
        return tweets.size();
    }

    // define viewholder (view holder for itemview <=> activity for view)
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;

        // itemView: representation of one row in recycler view
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
        }

        public void bind(Tweet tweet)
        {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .into(ivProfileImage);
        }
    }
}
