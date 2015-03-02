package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.util.SQLiteUtils;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vibhalaljani on 2/28/15.
 */
public class SearchTimelineFragment extends TweetsListFragment {
    private static final String TAG = "MENTIONSTIMELINEFRAGMENT";
    private String query;
    private String result_type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        query = getArguments().getString(Constants.queryKey);
        result_type = getArguments().getString(Constants.resultTypeKey);
        populateTimeline(Constants.max_id_first_req);
    }

    public static SearchTimelineFragment newInstance(String query, String result_type) {
        SearchTimelineFragment searchTimelineFragment = new SearchTimelineFragment();
        Bundle args = new Bundle();
        args.putString(Constants.queryKey, query);
        args.putString(Constants.resultTypeKey, result_type);
        searchTimelineFragment.setArguments(args);
        return searchTimelineFragment;
    }

    public void repopulate(String query, String result_type) {
        this.query = query;
        this.result_type = result_type;
        clear();
        this.populateTimeline(Constants.max_id_first_req);
    }


    // send an API request to get a timeline JSON and fill the list view
    public void populateTimeline(final long max_id) {
        // Call this API only if network is available
        if (Constants.isNetworkAvailable(getActivity())) {
            client.getSearchTimeline(query,
                                     result_type,
                                     max_id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    if (response != null) {
                        ArrayList<Tweet> tweets = null;
                        try {
                            tweets = Tweet.fromJSONArray(response.getJSONArray("statuses"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Populate list with tweet
                        if (tweets != null && tweets.size() > 0) {
                            if (max_id != Constants.max_id_first_req) {
                                tweets.remove(0);
                            }

                            addAll(tweets);
                        }
                    } else {
                        Log.e(TAG, Constants.jsonError + Constants.tweetsArrEmpty);
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.sth_wrong),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                    Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                            + " JSONObject: " + e.toString());
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.sth_wrong),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.internet_error),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
