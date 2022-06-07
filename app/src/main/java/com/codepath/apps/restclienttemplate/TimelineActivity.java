package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        // find recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // init list of tweets & the adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        // recycler view setup: layout manager & the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this)); //set layout manager
        rvTweets.setAdapter(adapter); //set adapter for rv

        populateHomeTimeline();
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
                startActivity(i_compose);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
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
}