package com.codepath.apps.restclienttemplate.utils;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
//import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import okhttp3.Headers;
import okhttp3.internal.http2.Header;

public class TweetDataSource extends ItemKeyedDataSource<Long, Tweet>
{
    // Pass whatever dependencies (params?) are needed to make the network call
    TwitterClient mClient;

    // Define the type of data that will be emitted by this datasource
    public TweetDataSource(TwitterClient client)
    {
        mClient = client;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Tweet> callback)
    {
        // Fetch data synchronously (second parameter is set to true)
        // load an initial data set so the paged list is not empty.
        // See https://issuetracker.google.com/u/2/issues/110843692?pli=1
        JsonHttpResponseHandler jsonHttpResponseHandler = createTweetHandler(callback, true);

        // No max_id should be passed on initial load
        mClient.getHomeTimeline(0, params.requestedLoadSize, jsonHttpResponseHandler);
    }

    // Called repeatedly when more data needs to be set
    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Tweet> callback)
    {
        // This network call can be asynchronous (second parameter is set to false)
        JsonHttpResponseHandler jsonHttpResponseHandler = createTweetHandler(callback, false);

        // params.key & requestedLoadSize should be used
        // params.key will be the lowest Twitter post ID retrieved and should be used for the max_id= parameter in Twitter API.
        // max_id = params.key - 1
        mClient.getHomeTimeline(params.key - 1, params.requestedLoadSize, jsonHttpResponseHandler);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Tweet> callback)
    {

    }

    public JsonHttpResponseHandler createTweetHandler(final LoadCallback<Tweet> callback, boolean isAsync)
    {

        JsonHttpResponseHandler handler = new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON response)
            {
                JSONArray jsonArray = response.jsonArray;   // get array of tweets
                ArrayList<Tweet> tweets = new ArrayList<>();
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // send back to PagedList handler
                callback.onResult(tweets);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        };

        if (isAsync) {
            // Fetch data synchronously
            // For AsyncHttpClient, this workaround forces the callback to be run synchronously

            handler.setUseSynchronousMode(true);
            handler.setUsePoolThread(true);
        }
    }

    // First type of ItemKeyedDataSource should match return type of getKey()
    @NonNull
    @Override
    public Long getKey(@NonNull Tweet item)
    {
        // item.getPostId() is a Long type
        return Long.valueOf(item.id);
    }
}
