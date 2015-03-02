package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
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
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.dialogs.ReplyDialog;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

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
    private ImageButton btnFav;
    private ImageButton btnRetweet;

    private Tweet curr_tweet;
    private User curr_user;

    private static final String fontHtmlBeg = "<font color=\"" + Constants.black + "\"><b>";
    private static final String fontHtmlEnd = "</b></font>";

    private static final String TAG = "TWEETDETAILACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        curr_tweet = getIntent().getParcelableExtra(Constants.tweetKey);
        curr_user = getIntent().getParcelableExtra(Constants.userKey);
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
        btnFav = (ImageButton) findViewById(R.id.btnFav);
        btnRetweet = (ImageButton) findViewById(R.id.btnRetweet);

        ivProfPic.setImageResource(0);
        if (curr_user.getProfile_image_url() != null) {
            Picasso.with(this).load(curr_user.getProfile_image_url()).into(ivProfPic);
        }
        tvUserName.setText(curr_user.getName());
        tvScreenName.setText(Constants.twitterUserRef + curr_user.getScreen_name());
        tvTweet.setText(curr_tweet.getText());



        if (curr_tweet.getRetweet_count() > 0) {
            String retweetStrHtml = "";
            retweetStrHtml = fontHtmlBeg + curr_tweet.getRetweet_count() + fontHtmlEnd + " " +
                    getResources().getString(R.string.retweets);
            tvRetweet.setText(Html.fromHtml(retweetStrHtml));
        }
        if (curr_tweet.isRetweeted()) {
            btnRetweet.setImageResource(R.drawable.ic_retweeted);
            btnRetweet.setEnabled(false);
        }
        if (curr_user.getScreen_name().equals(Constants.currentUser.getScreen_name())) {
            btnRetweet.setAlpha(Constants.retweetDisableAlpha);
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

        if (curr_tweet.isFavorited()) {
            btnFav.setImageResource(R.drawable.ic_favorited);
        }

        ivMedia.setImageResource(0);
        if (curr_tweet.getMedia_url() != null) {
            Picasso.with(this).load(curr_tweet.getMedia_url()).into(ivMedia);
        }

        tvCreatedAt.setText(getPrettyDate(curr_tweet.getCreated_at()));

        setupViewListeners();

    }

    private ReplyDialog replyDialog;
    private void showReplyDialog() {
        String replyDialogTag = "fragment_reply_dialog";
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        replyDialog = ReplyDialog.newInstance(curr_user.getScreen_name(),
                                              curr_tweet.getuid());
        replyDialog.show(fm, replyDialogTag);
    }

    private void setupViewListeners() {
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   showReplyDialog();
            }
        });
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = TwitterApplication.getRestClient();
                if (curr_tweet.isFavorited()) {
                    if (Constants.isNetworkAvailable(TweetDetailActivity.this)) {
                        client.defavorite(curr_tweet.getuid(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                if (response != null) {
                                    curr_tweet.setFavorited(false);
                                    btnFav.setImageResource(R.drawable.ic_fav);
                                    curr_tweet.setFavorites_count(curr_tweet.getFavorites_count() - 1);
                                    if (curr_tweet.getFavorites_count() > 0) {
                                        String favStrHtml = "";
                                        favStrHtml = fontHtmlBeg + curr_tweet.getFavorites_count()
                                                     + fontHtmlEnd + " " +
                                                getResources().getString(R.string.favorites);
                                        tvFav.setText(Html.fromHtml(favStrHtml));
                                    } else {
                                        tvFav.setText("");
                                        if (curr_tweet.getRetweet_count() == 0 &&
                                                curr_tweet.getFavorites_count() == 0) {
                                            divAboveCount.setBackgroundColor(getResources().
                                                    getColor(R.color.transparent_white));
                                        }
                                    }
                                    Tweet isExistingTweet = new Select().from(Tweet.class).
                                            where("uid = ?", curr_tweet.getuid()).executeSingle();
                                    if (isExistingTweet != null) {
                                        curr_tweet.save();
                                    }
                                } else {
                                    Log.e(TAG, Constants.jsonError + Constants.defavResponseNull);
                                    Toast.makeText(TweetDetailActivity.this,
                                            getResources().getString(R.string.sth_wrong),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers,
                                                  Throwable t, JSONObject e) {
                                Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                                        + " JSONObject: " + e.toString());
                                Toast.makeText(TweetDetailActivity.this,
                                        getResources().getString(R.string.sth_wrong),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(TweetDetailActivity.this,
                                getResources().getString(R.string.internet_error),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (Constants.isNetworkAvailable(TweetDetailActivity.this)) {
                        client.favorite(curr_tweet.getuid(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                if (response != null) {
                                    curr_tweet.setFavorited(true);
                                    curr_tweet.setFavorites_count(curr_tweet.getFavorites_count() + 1);
                                    btnFav.setImageResource(R.drawable.ic_favorited);
                                    String favStrHtml = "";
                                    favStrHtml = fontHtmlBeg + curr_tweet.getFavorites_count()
                                            + fontHtmlEnd + " " +
                                            getResources().getString(R.string.favorites);
                                    tvFav.setText(Html.fromHtml(favStrHtml));
                                    divAboveCount.setBackgroundColor(getResources().
                                            getColor(R.color.dark_gray));
                                    Tweet isExistingTweet = new Select().from(Tweet.class).
                                            where("uid = ?", curr_tweet.getuid()).executeSingle();
                                    if (isExistingTweet != null) {
                                        curr_tweet.save();
                                    }
                                } else {
                                    Log.e(TAG, Constants.jsonError + Constants.favResponseNull);
                                    Toast.makeText(TweetDetailActivity.this,
                                            getResources().getString(R.string.sth_wrong),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers,
                                                  Throwable t, JSONObject e) {
                                Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                                        + " JSONObject: " + e.toString());
                                Toast.makeText(TweetDetailActivity.this,
                                        getResources().getString(R.string.sth_wrong),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(TweetDetailActivity.this,
                                getResources().getString(R.string.internet_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = TwitterApplication.getRestClient();
                if (Constants.isNetworkAvailable(TweetDetailActivity.this)) {
                    client.retweet(curr_tweet.getuid(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode,
                                              Header[] headers,
                                              JSONObject response) {

                            if (response != null) {
                                curr_tweet.setRetweeted(true);
                                btnRetweet.setImageResource(R.drawable.ic_retweeted);
                                curr_tweet.setRetweet_count(curr_tweet.getRetweet_count() + 1);
                                tvRetweet.setTextColor(getResources().
                                        getColor(R.color.retweet_green));
                                tvRetweet.setText(String.valueOf(
                                        curr_tweet.getRetweet_count()));
                                Tweet isExistingTweet = new Select().from(Tweet.class).
                                        where("uid = ?", curr_tweet.getuid()).executeSingle();
                                if (isExistingTweet != null) {
                                    curr_tweet.save();
                                }
                            } else {
                                Log.e(TAG, Constants.jsonError + Constants.defavResponseNull);
                                Toast.makeText(TweetDetailActivity.this,
                                        getResources().getString(R.string.sth_wrong),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              Throwable t, JSONObject e) {
                            Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                                    + " JSONObject: " + e.toString());
                            Toast.makeText(TweetDetailActivity.this,
                                    getResources().getString(R.string.sth_wrong),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(TweetDetailActivity.this,
                            getResources().getString(R.string.internet_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Callback method for when reply is submitted from tweet detail activity
    public void onFinishReplyDialog(Tweet newTweet) {

        replyDialog.dismiss();

        Intent i = new Intent(TweetDetailActivity.this, HomeTimelineActivity.class);
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
