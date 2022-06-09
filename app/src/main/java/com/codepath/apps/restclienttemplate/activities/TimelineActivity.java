package com.codepath.apps.restclienttemplate.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity
{
    public static final String TAG = "TimelineActivity"; // for Log
    public final int REQUEST_CODE = 20; // startActivityForResult(): to determine result type later

    RecyclerView rvTweets;

    TwitterClient client;
    List<Tweet> tweets;

    LinearLayoutManager layoutManager;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;

    // ref: endless scroll example project: https://gist.github.com/rogerhu/17aca6ad4dbdb3fa5892
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        //SETUP: recycler view
        // find recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // init list of tweets & the adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        // recycler view setup: layout manager & the adapter
        layoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(layoutManager); //set layout manager
        rvTweets.setAdapter(adapter); //set adapter for rv

        populateHomeTimeline();

        //SETUP: swipe to refresh
        // find swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // set up refresh listener to trigger new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            // actions when refresh
            @Override
            public void onRefresh()
            {
                // refresh list
                fetchTimelineAsync(0);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //SETUP: infinite scroll
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        String max_id = tweets.get(tweets.size()-1).postId;

        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        client.getHomeTimeline(max_id, TwitterClient.NO_PAGE, new JsonHttpResponseHandler() //testing: no pages
        {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json)
            {
                Log.i(TAG, "infinite scroll: fetch tweets onSuccess " + json.toString());

                int preloadSize = adapter.getItemCount();

                //  --> Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;   // get array of tweets
                try {
                    //  --> Append the new data objects to the existing set of items inside the array of items
                    tweets.addAll(Tweet.fromJsonArray(jsonArray)); // adapter's 'tweets' points to this.tweets
                    //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
                    adapter.notifyItemRangeInserted(preloadSize, tweets.size() - 1);
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable)
            {
                Log.e(TAG, "infinite scroll: fetch tweets onFailure " + response, throwable); //error response from server, throwable: print out the exception
            }
        });
    }

    // initially populate timeline
    private void populateHomeTimeline()
    {
        client.getHomeTimeline(new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json)
            {
                Log.i(TAG, "onSuccess " + json.toString());
                JSONArray jsonArray = json.jsonArray;   // get array of tweets
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray)); // adapter's 'tweets' points to this.tweets
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable)
            {
                Log.e(TAG, "onFailure " + response, throwable); //error response from server, throwable: print out the exception
            }
        });
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json)
            {
                Log.i(TAG, "onSuccess " + json.toString());
                JSONArray jsonArray = json.jsonArray;   // get array of tweets

                // Remember to CLEAR OUT old items before appending in the new ones
                adapter.clear();

                try {
                    // add new items to your adapter
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
//                    adapter.notifyDataSetChanged(); //qq: why is this unnecessary
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable)
            {
                Log.d(TAG, "Fetch timeline error on refresh: " + throwable.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //return true for menu to be displayed (see doc of overriden method)
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // if id of menu item clicked == ?
        switch (item.getItemId())
        {
            // logout item clicked
            case R.id.logout:
                // forget who's logged in
                TwitterApp.getRestClient(this).clearAccessToken();

                // navigate backwards to login activity
                //== OPTION 1 ==//
                Intent i_logout = new Intent(this, LoginActivity.class); // params: origin, destination
                i_logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this makes sure the Back button won't work; close activities (TimelineActivity) on top of existing LoginActivity | ref: https://riptutorial.com/android/example/2736/clearing-an-activity-stack
                i_logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // clear ALL activites on top of existing LoginActivity
                startActivity(i_logout);

//                //== OPTION 2 ==//
//                finish();
                // return true to "consume" here (see docs) qq: what is consume?
                return true;
            case R.id.compose:
                // navigate to compose activity
                Intent i_compose = new Intent(this, ComposeActivity.class);
                startActivityForResult(i_compose, REQUEST_CODE);
//                launcher.launch(i_compose); // ALT: new way of doing startAcitivtyForResult()
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // request code == REQUEST_CODE; resultCode = if child activity finished successfully
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // get data from intent (tweet)
            Tweet newTweet = data.getParcelableExtra(data.getParcelableExtra("tweet"));

            // update rv with newly composed tweet
            // modify data source of tweets
            tweets.add(0, newTweet);
            // update rv adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0); // so no need scroll up to see new tweet
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //    // ALT: new way of doing startActivityForResult()
//    // src: https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
//    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>()
//            {
//                @Override
//                public void onActivityResult(ActivityResult result)
//                {
//                    if (result.getResultCode() == Activity.RESULT_OK)
//                    {
//                        Intent data = result.getData();
//                        // do sth with result
//                    }
//                }
//            }
//    );
}