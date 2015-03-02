package com.codepath.apps.mysimpletweets.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.codepath.apps.mysimpletweets.models.User;

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
    public static User currentUser;
    public static final String defaultBgImgUrl = "http://abs.twimg.com/images/themes/theme";
    public static final String topTweetsKey = "popular";
    public static final String allTweetsKey = "mixed";
    public static final String resultTypeKey = "result_type";
    public static final String queryKey = "query";
    public static final String favResponseNull = " Favorite response null.";
    public static final String defavResponseNull = " Defavorite response null.";
    public static final String screenNameKey = "screen_name";
    public static final String currTweetUidKey = "curr_status_uid";
    public static final String tweetsArrEmpty = " Tweets array null or empty";
    public static final float retweetDisableAlpha = 0.60F;
    public static final float retweetEnableAlpha = 1.0F;

    // This method determines if the mobile device is connected to the internet
    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


}
