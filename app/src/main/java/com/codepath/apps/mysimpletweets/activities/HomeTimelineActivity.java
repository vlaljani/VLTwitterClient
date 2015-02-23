package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.TweetsAdapter;
import com.codepath.apps.mysimpletweets.dialogs.ComposeDialog;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.helpers.EndlessScrollListener;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import android.util.Log;
import android.widget.Toast;

public class HomeTimelineActivity extends ActionBarActivity implements ComposeDialog.ComposeDialogListener {

    private ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<Tweet> tweetArrayList;
    private TweetsAdapter tweetArrayAdapter;

    private TwitterClient client;

    // Tag to mark the logs
    private static final String TAG = "HOMETIMELINEACTIVITY";

    private long curr_lowest_max_id = Constants.max_id_first_req;

    // Currently authenticated user
    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);
        setupViews();

        client = TwitterApplication.getRestClient();
        getCurrentUser();
        populateTimeline(Constants.max_id_first_req);
    }

    // Method to get the currently authenticated user
    private void getCurrentUser() {
        // Checking if the network is available
        if (Constants.isNetworkAvailable(HomeTimelineActivity.this)) {
            client.getCurrentUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // Delete any old authenticated users because there should be only one
                    new Delete().from(User.class).where("is_current_user = ?", true).execute();
                    currentUser = User.fromJson(response);
                    currentUser.setIs_current_user(true);
                    currentUser.save();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                    Toast.makeText(HomeTimelineActivity.this,
                            getResources().getString(R.string.sth_wrong),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, Constants.jsonError + " Throwable: " + t.toString() +
                            " JSONObject:  " + e.toString());

                }
            });
        } else {
            Toast.makeText(this, getResources().getString(R.string.internet_error),
                    Toast.LENGTH_SHORT).show();
            currentUser = new Select()
                        .from(User.class)
                        .where("is_current_user = ?", true)
                        .executeSingle();
        }

    }

    private void setupViews() {
        lvTweets = (ListView) findViewById(R.id.lvTweets);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(R.color.twitter_blue,
                                               R.color.white,
                                               R.color.twitter_blue,
                                               R.color.white);

        tweetArrayList = new ArrayList<>();
        tweetArrayAdapter = new TweetsAdapter(this,tweetArrayList);
        lvTweets.setAdapter(tweetArrayAdapter);

        setupViewListeners();
    }

    private void setupViewListeners() {
        /* Algorithm for pagination: If it's the first request, send only count. With every request
         * keep track of the lowest tweet id returned. Use that as the max_id in the next request.
         * For requests after the first, remove the first tweet returned because API requests with
         * max_id are inclusive.
         */
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // No point scrolling further if we don't have network
                if (Constants.isNetworkAvailable(HomeTimelineActivity.this)) {
                    populateTimeline(curr_lowest_max_id);
                } else {
                    Toast.makeText(HomeTimelineActivity.this,
                            getResources().getString(R.string.internet_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Constants.isNetworkAvailable(HomeTimelineActivity.this)) {
                    Intent i = new Intent(HomeTimelineActivity.this, TweetDetailActivity.class);
                    i.putExtra(Constants.tweetKey, tweetArrayList.get(position));
                    i.putExtra(Constants.userKey, tweetArrayList.get(position).getUser());
                    i.putExtra(Constants.authUserKey, currentUser);
                    startActivityForResult(i, Constants.TWEET_DETAIL_REQ_CODE);
                } else {
                    Toast.makeText(HomeTimelineActivity.this,
                            getResources().getString(R.string.internet_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // No point refreshing if we don't have data
                if (Constants.isNetworkAvailable(HomeTimelineActivity.this)) {
                    curr_lowest_max_id = Constants.max_id_first_req;
                    tweetArrayList.clear();
                    new Delete().from(Tweet.class).execute();
                    new Delete().from(User.class).where("is_current_user = ?", false).execute();
                    tweetArrayAdapter.notifyDataSetChanged();
                    populateTimeline(Constants.max_id_first_req);
                } else {
                    Toast.makeText(HomeTimelineActivity.this,
                            getResources().getString(R.string.internet_error),
                            Toast.LENGTH_SHORT).show();
                }
                swipeContainer.setRefreshing(false);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if we're back from the Tweet Detail activity
        if (resultCode == RESULT_OK && requestCode == Constants.TWEET_DETAIL_REQ_CODE) {
            Tweet newTweet = data.getParcelableExtra(Constants.newTweetKey);
            Log.i("new tweet", "home " + (newTweet.getUser() == null));
            newTweet.setUser(currentUser);
            newTweet.save();
            SQLiteUtils.execSql("UPDATE Tweets SET user=(SELECT Id FROM Users " +
                    "WHERE uid=" + currentUser.getUid() + ")"
                    +  " WHERE uid = "
                    + newTweet.getuid());

            ArrayList<Tweet> temp = new ArrayList<>();
            temp.add(newTweet);
            temp.addAll(tweetArrayList);
            tweetArrayList.clear();
            tweetArrayList.addAll(temp);
            tweetArrayAdapter.notifyDataSetChanged();
        }
    }

    // send an API request to get a timeline JSON and fill the list view
    private void populateTimeline(final long max_id) {
        // Call this API only if network is available
        if (Constants.isNetworkAvailable(this)) {
            client.getHomeTimeline(max_id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                    if (response != null) {
                        ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);
                        // Populate list with tweet
                        if (tweets != null && tweets.size() > 0) {
                            if (curr_lowest_max_id != Constants.max_id_first_req) {
                                tweets.remove(0);
                            }

                            tweetArrayList.addAll(tweets);
                            tweetArrayAdapter.notifyDataSetChanged();

                            for (int i = 0; i < tweets.size(); i++) {
                                User user_to_save = tweets.get(i).getUser();
                                User.saveIf(user_to_save);
                                Tweet.saveIf(tweets.get(i));

                                SQLiteUtils.execSql("UPDATE Tweets SET user=(SELECT Id FROM Users " +
                                        "WHERE uid=" + user_to_save.getUid() + ")"
                                         +  " WHERE uid = "
                                        + tweets.get(i).getuid());
                            }

                        }
                        curr_lowest_max_id = tweets.get(0).getuid();
                        for (int i = 1; i < tweets.size(); i++) {
                            if (tweets.get(i).getuid() < curr_lowest_max_id) {
                                curr_lowest_max_id = tweets.get(i).getuid();
                            }
                        }
                    } else {
                        Log.e(TAG, Constants.jsonError + "  Tweets array null or empty");
                        Toast.makeText(HomeTimelineActivity.this,
                                getResources().getString(R.string.sth_wrong),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                    Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                            + " JSONObject: " + e.toString());
                    Toast.makeText(HomeTimelineActivity.this,
                            getResources().getString(R.string.sth_wrong),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else { // Otherwise get the data from the DB
            // Theoretically, the only way to get here is if the user opens the app after closing
            // it because all other refresh scenarios (pull down/load more) prevent
            // populateTimeline from being called
            Toast.makeText(HomeTimelineActivity.this,
                    getResources().getString(R.string.internet_error),
                    Toast.LENGTH_SHORT).show();

            tweetArrayList.clear();
            tweetArrayList.addAll(Tweet.getAll());
            tweetArrayAdapter.notifyDataSetChanged();
        }

    }

    private void showComposeDialog() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        ComposeDialog composeDialog = ComposeDialog.newInstance(currentUser);
        composeDialog.show(fm, "fragment_all_comments");
    }

    public void onFinishComposeDialog(Tweet newTweet) {
        // Adding it manually because it's too soon to expect refresh to pick it up
        newTweet.save();
        SQLiteUtils.execSql("UPDATE Tweets SET user=(SELECT Id FROM Users " +
                "WHERE uid=" + currentUser.getUid() + ")"
                +  " WHERE uid = "
                + newTweet.getuid());
        ArrayList<Tweet> temp = new ArrayList<>();
        temp.add(newTweet);
        temp.addAll(tweetArrayList);
        tweetArrayList.clear();
        tweetArrayList.addAll(temp);
        tweetArrayAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // On compose icon click show the modal overlay compose dialog
        if (id == R.id.compose) {
            if (Constants.isNetworkAvailable(this)) {
                showComposeDialog();
            } else {
                Toast.makeText(HomeTimelineActivity.this,
                        getResources().getString(R.string.internet_error),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
