package com.codepath.apps.restclienttemplate;

import android.content.Context;

import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.oauth.OAuthBaseClient;
import com.github.scribejava.apis.FlickrApi;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/scribejava/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final BaseApi REST_API_INSTANCE = TwitterApi.instance();
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;       // Change this inside apikey.properties
	public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET; // Change this inside apikey.properties

	// Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
	public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";

	// See https://developer.chrome.com/multidevice/android/intents
	public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";

	public static final int DEFAULT_LOAD_SIZE = 25;
	public static final int NO_MAX_ID = -1;

	public TwitterClient(Context context) {
		super(context, REST_API_INSTANCE,
				REST_URL,
				REST_CONSUMER_KEY,
				REST_CONSUMER_SECRET,
				null,  // OAuth2 scope, null for OAuth1
				String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
						context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
	}

//	public void loadMoreTwitter(long max_id, JsonHttpResponseHandler handler)
//	{
//		getHomeTimeline(max_id, handler);
//	}

	// default overloaded method (for initial load in getHomeTimeline() & refresh in fetchTimelineAsync())
	public void getHomeTimeline(JsonHttpResponseHandler handler) { getHomeTimeline(NO_MAX_ID, handler); }

	// DEFINE METHODS for different API endpoints here
	// API ref: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/timelines/api-reference/get-statuses-home_timeline
	public void getHomeTimeline(long max_id, JsonHttpResponseHandler handler)
	{
		// GET ""
		String apiUrl = getApiUrl("statuses/home_timeline.json");

		// Can specify query string params directly or through RequestParams. (HTTPS request headers)
		RequestParams params = new RequestParams();
		params.put("count", DEFAULT_LOAD_SIZE);
		if (max_id != NO_MAX_ID)
		{
			params.put("max_id", max_id);
		}
//		if (page != NO_PAGE)
//		{
//			params.put("page", page);
//		}

		// make https request
		// get: HTTPS operation
		client.get(apiUrl, params, handler); // client: protected field var from OAuthBaseClient | handler: postman (handles HTTPS response on success/failure)
	}

	// API ref: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/post-and-engage/api-reference/post-statuses-update
	// similar structure to getHomeTimeline()
	public void publishTweet(String tweetContent, JsonHttpResponseHandler handler)
	{
		String apiUrl = getApiUrl("statuses/update.json");

		RequestParams params = new RequestParams();
		params.put("status", tweetContent);

		// post: HTTPS operation
		client.post(apiUrl, params, "", handler);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}
