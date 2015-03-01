package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.activeandroid.util.SQLiteUtils;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.dialogs.ReplyDialog;
import com.codepath.apps.mysimpletweets.fragments.TweetsListFragment;
import com.codepath.apps.mysimpletweets.fragments.UserHeaderFragment;
import com.codepath.apps.mysimpletweets.fragments.UserTimelineFragment;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;

import java.util.ArrayList;

public class ProfileActivity extends ActionBarActivity
                             implements TweetsListFragment.OnItemSelectedListener,
                                        ReplyDialog.ReplyDialogListener{

    private static final String TAG = "PROFILEACTIVITY";
    private UserTimelineFragment fragmentUserTimeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get the uid
        User curr_user = getIntent().getParcelableExtra(Constants.userKey);
        if (curr_user == null) {
            curr_user = Constants.currentUser;
        }

        String screen_name = curr_user.getScreen_name();
        getSupportActionBar().setTitle(Constants.twitterUserRef + screen_name);

        if (savedInstanceState == null) {
            // Create the user timeline fragment
            fragmentUserTimeline = UserTimelineFragment.newInstance(screen_name);
            UserHeaderFragment fragmentUserHeader = UserHeaderFragment.
                                                                newInstance(curr_user);

            // Display user fragments - header and timeline
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragProfileHeader, fragmentUserHeader);
            ft.replace(R.id.fragUserTimeline, fragmentUserTimeline);
            ft.commit();
        }

    }

    public void onTweetItemSelected(Tweet tweet, User user) {
        if (Constants.isNetworkAvailable(ProfileActivity.this)) {
            Intent i = new Intent(ProfileActivity.this, TweetDetailActivity.class);
            i.putExtra(Constants.tweetKey, tweet);
            i.putExtra(Constants.userKey, user);
            startActivityForResult(i, Constants.TWEET_DETAIL_REQ_CODE);
        } else {
            Toast.makeText(ProfileActivity.this,
                    getResources().getString(R.string.internet_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if we're back from the Tweet Detail activity
        if (resultCode == RESULT_OK && requestCode == Constants.TWEET_DETAIL_REQ_CODE) {
            Tweet newTweet = data.getParcelableExtra(Constants.newTweetKey);
            newTweet.setUser(Constants.currentUser);
            newTweet.save();
            SQLiteUtils.execSql("UPDATE Tweets SET user=(SELECT Id FROM Users " +
                    "WHERE uid=" + Constants.currentUser.getUid() + ")"
                    + " WHERE uid = "
                    + newTweet.getuid());

            ArrayList<Tweet> temp = new ArrayList<>();
            temp.add(newTweet);
            temp.addAll(fragmentUserTimeline.getTweetArrayList());
            fragmentUserTimeline.clear();
            fragmentUserTimeline.addAll(temp);
        }
    }

    private ReplyDialog replyDialog;
    public void showReplyDialog(String screen_name, long tweet_uid) {
        String replyDialogTag = "fragment_reply_dialog";
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        replyDialog = ReplyDialog.newInstance(screen_name, tweet_uid);
        replyDialog.show(fm, replyDialogTag);
    }

    public void onFinishReplyDialog(Tweet newTweet) {
        replyDialog.dismiss();

        ArrayList<Tweet> temp = new ArrayList<>();
        temp.add(newTweet);
        temp.addAll(fragmentUserTimeline.getTweetArrayList());
        fragmentUserTimeline.clear();
        fragmentUserTimeline.addAll(temp);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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
}
