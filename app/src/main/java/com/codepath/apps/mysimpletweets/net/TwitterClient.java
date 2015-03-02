package com.codepath.apps.mysimpletweets.net;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "n5EklTVp0EFIEI9NO7QQfdjB7";       // Change this
	public static final String REST_CONSUMER_SECRET = "7yMwIhaARPvZmoAehI5w8a04c7qWHHsjYYut9iBS1Wi90Oudqc"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletweets"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	// CHANGE THIS
	// DEFINE METHODS for different API endpoints here

    public void getHomeTimeline(long max_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", Constants.tweetCount);
        if (max_id != Constants.max_id_first_req) {
            params.put("max_id", max_id);
        }
        client.get(apiUrl, params, handler);
    }

    public void getMentionsTimeline(long max_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", Constants.tweetCount);
        if (max_id != Constants.max_id_first_req) {
            params.put("max_id", max_id);
        }
        client.get(apiUrl, params, handler);
    }

    public void getUserTimeline(String screen_name, long max_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();
        params.put("screen_name", screen_name);
        params.put("count", Constants.tweetCount);
        if (max_id != Constants.max_id_first_req) {
            params.put("max_id", max_id);
        }
        client.get(apiUrl, params, handler);
    }

    public void tweet(String status, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", status);
        client.post(apiUrl, params, handler);
    }

    // Theoretically, I would've made only 1 function tweet and added in_reply_to_status_id if it
    // was valid. However, in the interest of time, I don't want to break existing working
    // functionality so separating this out :)
    public void reply(String status, long in_reply_to_status_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", status);
        params.put("in_reply_to_status_id", in_reply_to_status_id);
        client.post(apiUrl, params, handler);
    }

    public void getCurrentUser(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        Log.i("Client", "HERE 4");
        client.get(apiUrl, handler);
        Log.i("Client", "HERE 5");
    }

    public void favorite(long tweet_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/create.json");
        RequestParams params = new RequestParams();
        params.put("id", tweet_id);
        client.post(apiUrl, params, handler);
    }

    public void retweet(long tweet_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/retweet/" + tweet_id + ".json");
        //RequestParams params = new RequestParams();
        //params.put("id", tweet_id);
        client.post(apiUrl, handler);
    }

    public void defavorite(long tweet_id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/destroy.json");
        RequestParams params = new RequestParams();
        params.put("id", tweet_id);
        client.post(apiUrl, params, handler);
    }

    public void getSearchTimeline(String query,
                                  String result_type,
                                  long max_id,
                                  AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("search/tweets.json");
        RequestParams params = new RequestParams();
        params.put("q", query);
        params.put("result_type", result_type);
        if (max_id != Constants.max_id_first_req) {
            params.put("max_id", max_id);
            params.put("count", Constants.tweetCount);
        }
        client.get(apiUrl, params, handler);
    }

    // Each method in here is an end point

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}