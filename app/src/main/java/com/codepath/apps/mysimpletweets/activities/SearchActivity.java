package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.activeandroid.util.SQLiteUtils;
import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.dialogs.ReplyDialog;
import com.codepath.apps.mysimpletweets.fragments.SearchTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.TweetsListFragment;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;

import java.util.ArrayList;

public class SearchActivity extends ActionBarActivity
                            implements TweetsListFragment.OnItemSelectedListener,
                                        ReplyDialog.ReplyDialogListener{

    private static final String TAG = "SEARCHACTIVITY";
    private String query;
    private TweetsSearchPagerAdapter searchPagerAdapter;
    private SearchTimelineFragment fragmentTopTweets;
    private SearchTimelineFragment fragmentAllTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        //actionBar.setDisplayHomeAsUpEnabled(true);

        query = getIntent().getStringExtra(Constants.queryKey);

        fragmentTopTweets = SearchTimelineFragment.newInstance(query, Constants.topTweetsKey);
        fragmentAllTweets = SearchTimelineFragment.newInstance(query, Constants.allTweetsKey);
        searchPagerAdapter = new TweetsSearchPagerAdapter(getSupportFragmentManager());

        // Get the view pager (There's a view pager, and a view pager indicator, which displays
        // which page you're on in the sliding tabs)
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Set the view pager adapter for the pager
        viewPager.setAdapter(searchPagerAdapter);

        // Find the pager sliding tabs
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        // Attach the pager tabs to the view pager
        tabs.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // When a user submits a query on the action bar search view, invoke this.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                query = q;
                fragmentTopTweets.repopulate(query, Constants.topTweetsKey);
                fragmentAllTweets.repopulate(query, Constants.allTweetsKey);

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

        return super.onOptionsItemSelected(item);
    }

    // Callback if a tweet is selected in the listview in the user timeline to go into detailed view
    public void onTweetItemSelected(Tweet tweet, User user) {
        if (Constants.isNetworkAvailable(SearchActivity.this)) {
            Intent i = new Intent(SearchActivity.this, TweetDetailActivity.class);
            i.putExtra(Constants.tweetKey, tweet);
            i.putExtra(Constants.userKey, user);
            startActivityForResult(i, Constants.TWEET_DETAIL_REQ_CODE);
        } else {
            Toast.makeText(SearchActivity.this,
                    getResources().getString(R.string.internet_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if we're back from the Tweet Detail activity w/ a reply
        if (resultCode == RESULT_OK && requestCode == Constants.TWEET_DETAIL_REQ_CODE) {
            Tweet newTweet = data.getParcelableExtra(Constants.newTweetKey);
            tweetReplyGenericActions(newTweet);
        }
    }

    private ReplyDialog replyDialog;

    //Callback method for when reply button is clicked on a tweet in the listview
    public void showReplyDialog(String screen_name, long tweet_uid) {
        String replyDialogTag = "fragment_reply_dialog";
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        replyDialog = ReplyDialog.newInstance(screen_name, tweet_uid);
        replyDialog.show(fm, replyDialogTag);
    }

    // Callback method for when Tweet button is hit after replying to a tweet in the listview
    public void onFinishReplyDialog(Tweet newTweet) {
        replyDialog.dismiss();

        tweetReplyGenericActions(newTweet);
    }

    private void tweetReplyGenericActions(Tweet newTweet) {
        newTweet.setUser(Constants.currentUser);
        newTweet.save();
        SQLiteUtils.execSql("UPDATE Tweets SET user=(SELECT Id FROM Users " +
                "WHERE uid=" + Constants.currentUser.getUid() + ")"
                + " WHERE uid = "
                + newTweet.getuid());

        if (newTweet.getText().contains(query)) {
            ArrayList<Tweet> temp = new ArrayList<>();
            temp.add(newTweet);
            temp.addAll(fragmentAllTweets.getTweetArrayList());
            fragmentAllTweets.clear();
            fragmentAllTweets.addAll(temp);
        }
    }

    // Return the order of the fragments in the viewPager
    public class TweetsSearchPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] { getResources().getString(R.string.top_tweets),
                                                    getResources().getString(R.string.all_tweets) };

        // This is how the adapter gets manager to insert/remove fragments from activity
        public TweetsSearchPagerAdapter(FragmentManager fm) {
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
                return fragmentTopTweets;
            } else if (position == 1) {
                return fragmentAllTweets;
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
}
