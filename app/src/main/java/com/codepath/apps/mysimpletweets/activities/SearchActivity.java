package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.fragments.HomeTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.MentionsTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.SearchTimelineFragment;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.net.TwitterClient;

public class SearchActivity extends ActionBarActivity {

    private static final String TAG = "SEARCHACTIVITY";
    private String query;
    private TweetsSearchPagerAdapter searchPagerAdapter;
    private SearchTimelineFragment fragmentTopTweets;
    private SearchTimelineFragment fragmentAllTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
                Toast.makeText(SearchActivity.this, query, Toast.LENGTH_SHORT).show();
                fragmentTopTweets.repopulate(query, Constants.topTweetsKey);
                fragmentAllTweets.repopulate(query, Constants.allTweetsKey);
                //searchPagerAdapter.notifyDataSetChanged();
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
                // Edit: Doing this so that we can update the home timeline by referring to it
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
