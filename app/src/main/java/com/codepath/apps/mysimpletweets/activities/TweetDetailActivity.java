package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.dialogs.ComposeDialog;
import com.codepath.apps.mysimpletweets.dialogs.ReplyDialog;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TweetDetailActivity extends ActionBarActivity implements ReplyDialog.ReplyDialogListener{

    private ImageView ivProfPic;
    private TextView tvUserName;
    private TextView tvScreenName;
    private TextView tvTweet;
    private ImageView ivMedia;
    private TextView tvCreatedAt;
    private TextView tvRetweet;
    private TextView tvFav;

    private View divAboveCount;

    private ImageButton btnReply;

    private Tweet curr_tweet;
    private User curr_user;
    private User auth_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);
        curr_tweet = getIntent().getParcelableExtra(Constants.tweetKey);
        curr_user = getIntent().getParcelableExtra(Constants.userKey);
        auth_user = getIntent().getParcelableExtra(Constants.authUserKey);
        setupViews();
    }

    private void setupViews() {
        ivProfPic = (ImageView) findViewById(R.id.ivProfPic);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        tvTweet = (TextView) findViewById(R.id.tvTweet);
        ivMedia = (ImageView) findViewById(R.id.ivMedia);
        tvCreatedAt = (TextView) findViewById(R.id.tvCreatedAt);
        tvRetweet = (TextView) findViewById(R.id.tvRetweet);
        tvFav = (TextView) findViewById(R.id.tvFav);
        divAboveCount = findViewById(R.id.divAboveCount);
        btnReply = (ImageButton) findViewById(R.id.btnReply);

        ivProfPic.setImageResource(0);
        Picasso.with(this).load(curr_user.getProfile_image_url()).into(ivProfPic);
        tvUserName.setText(curr_user.getName());
        tvScreenName.setText(Constants.twitterUserRef + curr_user.getScreen_name());
        tvTweet.setText(curr_tweet.getText());

        String fontHtmlBeg = "<font color=\"" + Constants.black + "\"><b>";
        String fontHtmlEnd = "</b></font>";

        if (curr_tweet.getRetweet_count() > 0) {
            String retweetStrHtml = "";
            retweetStrHtml = fontHtmlBeg + curr_tweet.getRetweet_count() + fontHtmlEnd + " " +
                    getResources().getString(R.string.retweets);
            tvRetweet.setText(Html.fromHtml(retweetStrHtml));
        }

        if (curr_tweet.getFavorites_count() > 0) {
            String favStrHtml = "";
            favStrHtml = fontHtmlBeg + curr_tweet.getFavorites_count() + fontHtmlEnd + " " +
                    getResources().getString(R.string.favorites);
            tvFav.setText(Html.fromHtml(favStrHtml));
        }

        if (curr_tweet.getRetweet_count() == 0 && curr_tweet.getFavorites_count() == 0) {
            divAboveCount.setBackgroundColor(getResources().getColor(R.color.transparent_white));
        }

        ivMedia.setImageResource(0);
        Picasso.with(this).load(curr_tweet.getMedia_url()).into(ivMedia);

        tvCreatedAt.setText(getPrettyDate(curr_tweet.getCreated_at()));

        setupViewListeners();

    }

    private ReplyDialog replyDialog;
    private void showReplyDialog() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        replyDialog = ReplyDialog.newInstance(auth_user,
                                                          curr_user.getScreen_name(),
                                                          curr_tweet.getuid());
        replyDialog.show(fm, "fragment_all_comments");
    }

    private void setupViewListeners() {
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   showReplyDialog();
            }
        });
    }

    public void onFinishReplyDialog(Tweet newTweet) {

        replyDialog.dismiss();

        Intent i = new Intent(TweetDetailActivity.this, HomeTimelineActivity.class);
        Log.i("new tweet", "are you? " + (newTweet.getUser() == null));
        i.putExtra(Constants.newTweetKey, newTweet);

        setResult(RESULT_OK, i);
        finish();

    }

    private String getPrettyDate(String created_at) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.twitterFormat);
        Date d = null;
        try {
            d = sdf.parse(created_at);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern(Constants.twitterDetailFormat);
        return sdf.format(d);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_detail, menu);
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
