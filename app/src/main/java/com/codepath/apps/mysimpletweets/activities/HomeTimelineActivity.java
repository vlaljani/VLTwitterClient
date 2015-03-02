package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;
import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.R;

import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.dialogs.ComposeDialog;
import com.codepath.apps.mysimpletweets.dialogs.ReplyDialog;
import com.codepath.apps.mysimpletweets.fragments.HomeTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.MentionsTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.TweetsListFragment;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.util.Log;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeTimelineActivity extends ActionBarActivity
                                  implements ComposeDialog.ComposeDialogListener,
                                             TweetsListFragment.OnItemSelectedListener,
                                             ReplyDialog.ReplyDialogListener {

    // Tag to mark the logs
    private static final String TAG = "HOMETIMELINEACTIVITY";
    private TwitterClient client;
    private HomeTimelineFragment homeTimelineFragment;
    private MentionsTimelineFragment mentionsTimelineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);
        client = TwitterApplication.getRestClient();
        getCurrentUser();
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Get the view pager (There's a view pager, and a view pager indicator, which displays
        // which page you're on in the sliding tabs)
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Set the view pager adapter for the pager
        viewPager.setAdapter(new TweetsPagerAdapter(getSupportFragmentManager()));

        // Find the pager sliding tabs
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        // Attach the pager tabs to the view pager
        tabs.setViewPager(viewPager);
    }

    // This method is a callback from TweetListFragment if a tweet is clicked on in listview
    // to get a detailed view in a new activity
    public void onTweetItemSelected(Tweet tweet, User user) {
        if (Constants.isNetworkAvailable(HomeTimelineActivity.this)) {
            Intent i = new Intent(HomeTimelineActivity.this, TweetDetailActivity.class);
            i.putExtra(Constants.tweetKey, tweet);
            i.putExtra(Constants.userKey, user);
            startActivityForResult(i, Constants.TWEET_DETAIL_REQ_CODE);
        } else {
            Toast.makeText(HomeTimelineActivity.this,
                    getResources().getString(R.string.internet_error),
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if we're back from the Tweet Detail activity with a reply
        if (resultCode == RESULT_OK && requestCode == Constants.TWEET_DETAIL_REQ_CODE) {
            Tweet newTweet = data.getParcelableExtra(Constants.newTweetKey);
            tweetReplyGenericActions(newTweet);
        }
    }

    private ReplyDialog replyDialog;

    // Callback when user hits reply button on a tweet in the tweetsListFragment/tweetAdapter and we
    // need to open up a new dialog for user to type in the reply
    public void showReplyDialog(String screen_name, long tweet_uid) {
        String replyDialogTag = "fragment_reply_dialog";
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        replyDialog = ReplyDialog.newInstance(screen_name, tweet_uid);
        replyDialog.show(fm, replyDialogTag);
    }

    // Callback when user hits 'Tweet' in the reply dialog (for tweets in timeline as opposed to
    // tweet detail activity)
    public void onFinishReplyDialog(Tweet newTweet) {
        replyDialog.dismiss();

        tweetReplyGenericActions(newTweet);
    }

    // The set of actions that need to be performed for any reply (whether the reply was in detailed
    // view or in fragment)
    private void tweetReplyGenericActions(Tweet newTweet) {
        newTweet.setUser(Constants.currentUser);
        newTweet.save();
        SQLiteUtils.execSql("UPDATE Tweets SET user=(SELECT Id FROM Users " +
                "WHERE uid=" + Constants.currentUser.getUid() + ")"
                +  " WHERE uid = "
                + newTweet.getuid());

        // Add tweet to the home timeline
        ArrayList<Tweet> temp = new ArrayList<>();
        temp.add(newTweet);
        temp.addAll(homeTimelineFragment.getTweetArrayList());
        homeTimelineFragment.clear();
        homeTimelineFragment.addAll(temp);

        // Add tweet to mentions timeline, if there's a mention of the authenticated user
        if (newTweet.getText().contains(Constants.twitterUserRef +
                Constants.currentUser.getScreen_name())) {
            temp.clear();
            temp.add(newTweet);
            temp.addAll(mentionsTimelineFragment.getTweetArrayList());
            mentionsTimelineFragment.clear();
            mentionsTimelineFragment.addAll(temp);
        }
    }


    // Method invoked when we hit the compose icon
    private void showComposeDialog() {
        String showComposeDialogTag = "fragment_compose";
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        ComposeDialog composeDialog = ComposeDialog.newInstance(Constants.currentUser);
        composeDialog.show(fm, showComposeDialogTag);
    }

    // Listener callback when we hit Tweet on compose dialog
    public void onFinishComposeDialog(Tweet newTweet) {
        // Adding it manually because it's too soon to expect refresh to pick it up
        newTweet.save();
        SQLiteUtils.execSql("UPDATE Tweets SET user=(SELECT Id FROM Users " +
                "WHERE uid=" + Constants.currentUser.getUid() + ")"
                + " WHERE uid = "
                + newTweet.getuid());
        ArrayList<Tweet> temp = new ArrayList<>();
        temp.add(newTweet);
        temp.addAll(homeTimelineFragment.getTweetArrayList());
        homeTimelineFragment.clear();
        homeTimelineFragment.addAll(temp);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_timeline, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // When a user submits a query on the action bar search view, invoke this.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                Intent i = new Intent(HomeTimelineActivity.this, SearchActivity.class);
                i.putExtra(Constants.queryKey, q);
                startActivity(i);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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

    // Return the order of the fragments in the viewPager
    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] { getResources().getString(R.string.home),
                                                    getResources().getString(R.string.mentions) };

        // This is how the adapter gets manager to insert/remove fragments from activity
        public TweetsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        // Order and creation of fragments within pager
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                // Edit: Doing this so that we can update the home timeline by referring to it
                //return new HomeTimelineFragment();
                homeTimelineFragment = new HomeTimelineFragment();
                return homeTimelineFragment;
            } else if (position == 1) {
                mentionsTimelineFragment = new MentionsTimelineFragment();
                return mentionsTimelineFragment;
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }

    }

    public void onProfileView(MenuItem mi) {
        // Launch profile view
        Intent i = new Intent(HomeTimelineActivity.this, ProfileActivity.class);
        i.putExtra(Constants.userKey, Constants.currentUser);
        startActivity(i);
    }

    // Method to get the currently authenticated user
    private void getCurrentUser() {
        // Checking if the network is available
        Log.i(TAG, "goes here");
        if (Constants.isNetworkAvailable(this)) {
            client.getCurrentUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // Delete any old authenticated users because there should be only one
                    Log.i(TAG, "goes here 1");
                    Constants.currentUser = User.fromJson(response);
                    Log.i(TAG, "here 2");
                    Constants.currentUser.setIs_current_user(true);
                    Log.i(TAG, "here 3");
                    new Delete().from(User.class).where("is_current_user = ?", true).execute();
                    Constants.currentUser.save();
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
            /*client.getCurrentUser(new TextHttpResponseHandler() {
                                      @Override
                                      public void onStart() {
                                          // Initiated the request
                                      }

                                      public void onSuccess(int statusCode, Header[] header, String responseBody) {
                                          Log.i(TAG, responseBody);
                                      }

                                      public void onFailure(int statusCode, Header[] header, String responseBody, Throwable e) {
                                          // Response failed :(
                                          Log.i(TAG, responseBody + e.toString());
                                      }

                                      @Override
                                      public void onFinish() {
                                          // Completed the request (either success or failure)
                                      }
                                  }
            );*/
        } else {
            Toast.makeText(this, getResources().getString(R.string.internet_error),
                    Toast.LENGTH_SHORT).show();
            Constants.currentUser = new Select()
                    .from(User.class)
                    .where("is_current_user = ?", true)
                    .executeSingle();
        }
        Log.i(TAG, "hmm");
        //Log.i(TAG, Constants.currentUser.getScreen_name() + " is done");
    }
}
