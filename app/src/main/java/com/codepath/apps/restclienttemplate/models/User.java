package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel // contained inside Tweet => need to be parcelable as well
public class User
{
    public String name;
    public String screenName; //handle
    public String profileImageUrl;

    // FOR: Parceler
    public User() {
    }

    // populate User object from json Object
    public static User fromJson(JSONObject jsonObject) throws JSONException
    {
        User user = new User();
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImageUrl = jsonObject.getString("profile_image_url_https");
        return user;
    }
}
