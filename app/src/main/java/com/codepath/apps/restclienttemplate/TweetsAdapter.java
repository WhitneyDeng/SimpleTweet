package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// signature: extends: an adapter for recyclerview that holds tweets (viewholder)
public class TweetsAdapter extends PagedListAdapter<Tweet, TweetsAdapter.ViewHolder>
{
    Context context;
    List<Tweet> tweets;

    public static final String TAG = "TweetsAdapter";

    public static final DiffUtil.ItemCallback<Tweet> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Tweet>() {
                @Override
                public boolean areItemsTheSame(Tweet oldItem, Tweet newItem) {
                    return oldItem.id == newItem.id;
                }
                @Override
                public boolean areContentsTheSame(Tweet oldItem, Tweet newItem) {
                    return (Objects.equals(oldItem, newItem)); //orig: compare each field (but I'm too lazy)
                }
            };

    public TweetsAdapter()
    {
        super(DIFF_CALLBACK);
    }

//    // pass in context and list of tweets
//    public TweetsAdapter(Context context, List<Tweet> tweets)
//    {
//        this.context = context;
//        this.tweets = tweets;
//    }

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
        Tweet tweet = getItem(position);

        // null placeholders if the PagedList is configured to use them
        // only works for data sets that have total count provided (i.e. PositionalDataSource)
        if (tweet == null)
        {
            return;
        }

        // bind tweet data to view holder
        holder.bind(tweet);
    }

//    @Override
//    public int getItemCount()
//    {
//        return tweets.size();
//    }

    //FOR: swipe down to refresh
    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    //FOR: swipe down to refresh
    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    public void addMoreTweets(List<Tweet> newTweets)
    {
        tweets.addAll(newTweets);
        submitList((PagedList<Tweet>) tweets);
    }

    // define viewholder (view holder for itemview <=> activity for view)
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvName;
        TextView tvScreenName;
        TextView tvRelativeTimestamp;
        ImageView ivPostImage;

        // itemView: representation of one row in recycler view
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
        }

        public void bind(Tweet tweet)
        {
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvRelativeTimestamp.setText(getRelativeTimeAgo(tweet.createdAt));

            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .into(ivProfileImage);

            if (tweet.mediaUrlHttps.equals(Tweet.NO_MEDIA))
            {
                ivPostImage.setVisibility(View.GONE);
            }
            else
            {
                int radius = 30;
                Glide.with(context)
                        .load(tweet.mediaUrlHttps)
                        .centerCrop()                           // to round corners (note: must enable adjustViewBounds in xml)
                        .transform(new RoundedCorners(radius))  // to round corners
                        .into(ivPostImage);
            }
        }

        // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
        public String getRelativeTimeAgo(String rawJsonDate) {
            String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
            sf.setLenient(true); //what is leniency?: https://stackoverflow.com/questions/7606387/what-is-the-use-of-lenient

            try {
                long time = sf.parse(rawJsonDate).getTime();
                long now = System.currentTimeMillis();

                final long diff = now - time;
                if (diff < DateUtils.MINUTE_IN_MILLIS) {
                    return "just now";
                } else if (diff < 2 * DateUtils.MINUTE_IN_MILLIS) {
                    return "a minute ago";
                } else if (diff < 50 * DateUtils.MINUTE_IN_MILLIS) {
                    return diff / DateUtils.MINUTE_IN_MILLIS + " m";
                } else if (diff < 90 * DateUtils.MINUTE_IN_MILLIS) {
                    return "an hour ago";
                } else if (diff < 24 * DateUtils.HOUR_IN_MILLIS) {
                    return diff / DateUtils.HOUR_IN_MILLIS + " h";
                } else if (diff < 48 * DateUtils.HOUR_IN_MILLIS) {
                    return "yesterday";
                } else {
                    return diff / DateUtils.DAY_IN_MILLIS + " d";
                }
                // todo: if over a month ago, put date & month only
            } catch (ParseException e) {
                Log.i(TAG, "getRelativeTimeAgo failed");
                e.printStackTrace();
            }
            return "";
        }
    }
}
