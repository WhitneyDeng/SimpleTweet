package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel //to pass composed tweet back to timeline
public class Tweet
{
    public final static int MAX_BODY_LENGTH = 140;
    public final static int FIRST_ITEM = 0;
    public final static String NO_MEDIA = "no media in this post";

    public String body;
    public String createdAt;
    public User user;
    public String mediaUrlHttps;

    // FOR: Parceler library
    public Tweet() {
    }

    // populate Tweet object from json response
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException
    {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user")); //make a User object based on json Object

        JSONObject entities = jsonObject.getJSONObject("entities");
        if (entities.has("media"))
        {
            tweet.mediaUrlHttps = entities
                    .getJSONArray("media")
                    .getJSONObject(FIRST_ITEM) // first position
                    .getString("media_url_https");
        }
        else
        {
            tweet.mediaUrlHttps = NO_MEDIA;
        }

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException
    {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

}
