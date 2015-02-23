package com.codepath.apps.mysimpletweets.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by vibhalaljani on 2/19/15.
 */
public class Constants {
    public static final String jsonError = "Something went wrong while parsing the JSONObject";
    public static final int tweetCount = 25;
    public static final int max_id_first_req = -1;
    public static final String userKey = "user";
    public static final String twitterUserRef = "@";
    public static final String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
    public static final String twitterDetailFormat = "HH:mm a . dd MMM yy";
    public static final String tweetKey = "tweet";
    public static final String newTweetKey = "newTweet";
    public static final int TWEET_DETAIL_REQ_CODE = 7;
    public static final String black = "#000000";
    public static final String authUserKey = "authUser";

    // This method determines if the mobile device is connected to the internet
    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
