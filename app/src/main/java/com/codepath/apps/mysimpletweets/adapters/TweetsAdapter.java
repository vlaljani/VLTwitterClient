package com.codepath.apps.mysimpletweets.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.activities.HomeTimelineActivity;
import com.codepath.apps.mysimpletweets.activities.ProfileActivity;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;


import org.apache.http.Header;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

/**
 * Created by vibhalaljani on 2/19/15.
 *
 * Turn the tweet objects into view
 */
public class TweetsAdapter extends ArrayAdapter<Tweet> {
    private static final String TAG = "TWEETSADAPTER";

    private static class ViewHolder {
        ImageView ivProfPic;
        TextView tvUserName;
        TextView tvScreenName;
        TextView tvCreatedAt;
        TextView tvTweet;
        LinearLayout lvMedia;
        TextView tvRetweet;
        TextView tvFavorites;
        ImageView ivFav;
        ImageView ivReply;
        ImageView ivRetweet;
    }
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        super(context, android.R.layout.simple_list_item_1, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Tweet currTweet = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet,
                    parent, false);
            viewHolder.ivProfPic = (ImageView) convertView.findViewById(R.id.ivProfPic);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
            viewHolder.tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
            viewHolder.tvTweet = (TextView) convertView.findViewById(R.id.tvTweet);
            viewHolder.lvMedia = (LinearLayout) convertView.findViewById(R.id.lvMedia);
            viewHolder.tvRetweet = (TextView) convertView.findViewById(R.id.tvRetweet);
            viewHolder.tvFavorites = (TextView) convertView.findViewById(R.id.tvFav);
            viewHolder.ivFav = (ImageView) convertView.findViewById(R.id.ivFav);
            viewHolder.ivReply = (ImageView) convertView.findViewById(R.id.ivReply);
            viewHolder.ivRetweet = (ImageView) convertView.findViewById(R.id.ivRetweet);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof HomeTimelineActivity) {
                    ((HomeTimelineActivity)getContext()).showReplyDialog(
                                                             currTweet.getUser().getScreen_name(),
                                                             currTweet.getuid());
                } else if (getContext() instanceof ProfileActivity) {
                        ((ProfileActivity)getContext()).showReplyDialog(
                                currTweet.getUser().getScreen_name(),
                                currTweet.getuid());
                }

            }
        });

        viewHolder.ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = TwitterApplication.getRestClient();
                    if (Constants.isNetworkAvailable(getContext())) {
                        client.retweet(currTweet.getuid(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                if (response != null) {
                                    currTweet.setRetweeted(true);
                                    viewHolder.ivRetweet.setImageResource(R.drawable.ic_retweeted);
                                    currTweet.setRetweet_count(currTweet.getRetweet_count() + 1);
                                    viewHolder.tvRetweet.setTextColor(getContext().getResources().
                                                                   getColor(R.color.retweet_green));
                                    viewHolder.tvRetweet.setText(String.valueOf(
                                                                     currTweet.getRetweet_count()));
                                    Tweet isExistingTweet = new Select().from(Tweet.class).
                                            where("uid = ?", currTweet.getuid()).executeSingle();
                                    if (isExistingTweet != null) {
                                        currTweet.save();
                                    }
                                } else {
                                    Log.e(TAG, Constants.jsonError + Constants.defavResponseNull);
                                    Toast.makeText(getContext(),
                                            getContext().getResources().getString(R.string.sth_wrong),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers,
                                                  Throwable t, JSONObject e) {
                                Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                                        + " JSONObject: " + e.toString());
                                Toast.makeText(getContext(),
                                        getContext().getResources().getString(R.string.sth_wrong),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(),
                                getContext().getResources().getString(R.string.internet_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        });

        viewHolder.ivFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterClient client = TwitterApplication.getRestClient();
                if (currTweet.isFavorited()) {
                    if (Constants.isNetworkAvailable(getContext())) {
                        client.defavorite(currTweet.getuid(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                if (response != null) {
                                    currTweet.setFavorited(false);
                                    viewHolder.ivFav.setImageResource(R.drawable.ic_fav);
                                    currTweet.setFavorites_count(currTweet.getFavorites_count() - 1);
                                    viewHolder.tvFavorites.setTextColor(getContext().getResources().
                                            getColor(R.color.dark_gray));
                                    if (currTweet.getFavorites_count() > 0) {
                                        viewHolder.tvFavorites.setText(String.valueOf(
                                                currTweet.getFavorites_count()));
                                    } else {
                                        viewHolder.tvFavorites.setText("");
                                    }
                                    Tweet isExistingTweet = new Select().from(Tweet.class).
                                            where("uid = ?", currTweet.getuid()).executeSingle();
                                    if (isExistingTweet != null) {
                                        currTweet.save();
                                    }
                                } else {
                                    Log.e(TAG, Constants.jsonError + Constants.defavResponseNull);
                                    Toast.makeText(getContext(),
                                            getContext().getResources().getString(R.string.sth_wrong),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers,
                                                  Throwable t, JSONObject e) {
                                Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                                        + " JSONObject: " + e.toString());
                                Toast.makeText(getContext(),
                                        getContext().getResources().getString(R.string.sth_wrong),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(),
                                getContext().getResources().getString(R.string.internet_error),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (Constants.isNetworkAvailable(getContext())) {
                        client.favorite(currTweet.getuid(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                if (response != null) {
                                    currTweet.setFavorited(true);
                                    currTweet.setFavorites_count(currTweet.getFavorites_count() + 1);
                                    viewHolder.ivFav.setImageResource(R.drawable.ic_favorited);
                                    viewHolder.tvFavorites.setTextColor(getContext().getResources().
                                            getColor(R.color.favorite_orange));
                                    viewHolder.tvFavorites.setText(String.valueOf(
                                                                   currTweet.getFavorites_count()));
                                    Tweet isExistingTweet = new Select().from(Tweet.class).
                                               where("uid = ?", currTweet.getuid()).executeSingle();
                                    if (isExistingTweet != null) {
                                        currTweet.save();
                                    }
                                } else {
                                    Log.e(TAG, Constants.jsonError + Constants.favResponseNull);
                                    Toast.makeText(getContext(),
                                            getContext().getResources().getString(R.string.sth_wrong),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers,
                                                  Throwable t, JSONObject e) {
                                Log.e(TAG, Constants.jsonError + "  Throwable: " + t.toString()
                                        + " JSONObject: " + e.toString());
                                Toast.makeText(getContext(),
                                        getContext().getResources().getString(R.string.sth_wrong),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(),
                                getContext().getResources().getString(R.string.internet_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        viewHolder.tvUserName.setText(currTweet.getUser().getName());
        viewHolder.tvScreenName.setText(" " + Constants.twitterUserRef +
                                                    currTweet.getUser().getScreen_name());
        viewHolder.tvCreatedAt.setText(getRelativeTimeAgo(currTweet.getCreated_at()));
        viewHolder.tvTweet.setText(currTweet.getText());

        // Reset everything
        viewHolder.ivRetweet.setImageResource(R.drawable.ic_retweet);
        viewHolder.tvRetweet.setText("");
        viewHolder.tvRetweet.setTextColor(getContext().getResources().getColor(R.color.dark_gray));
        viewHolder.ivRetweet.setEnabled(true);
        viewHolder.ivRetweet.setAlpha(Constants.retweetEnableAlpha);

        // If current user's tweet, disable the retweet button
        Log.i(TAG, "curr_user " + currTweet.getUser().getScreen_name());
        Log.i(TAG, "auth_user " + Constants.currentUser.getScreen_name());
        if (currTweet.getUser().getScreen_name().equals(Constants.currentUser.getScreen_name())) {
            viewHolder.ivRetweet.setEnabled(false);
            viewHolder.ivRetweet.setAlpha(Constants.retweetDisableAlpha);
        }
        if (currTweet.isRetweeted()) {
            viewHolder.ivRetweet.setImageResource(R.drawable.ic_retweeted);
            viewHolder.tvRetweet.setTextColor(getContext().getResources().getColor(
                    R.color.retweet_green));
            viewHolder.ivRetweet.setEnabled(false);
        }
        if (currTweet.getRetweet_count() > 0) {
            viewHolder.tvRetweet.setText(String.valueOf(currTweet.getRetweet_count()));
        } else {
            viewHolder.tvRetweet.setText("");
        }

        viewHolder.ivFav.setImageResource(R.drawable.ic_fav);
        viewHolder.tvFavorites.setText("");
        viewHolder.tvFavorites.setTextColor(getContext().getResources().getColor(R.color.dark_gray));
        if (currTweet.isFavorited()) {
            viewHolder.ivFav.setImageResource(R.drawable.ic_favorited);
            viewHolder.tvFavorites.setTextColor(getContext().getResources().
                    getColor(R.color.favorite_orange));
        }
        if (currTweet.getFavorites_count() > 0) {
            viewHolder.tvFavorites.setText(String.valueOf(currTweet.getFavorites_count()));
        } else {
            viewHolder.tvFavorites.setText("");
        }

        if (currTweet.getUser().getProfile_image_url() != null) {
            viewHolder.ivProfPic.setImageResource(0);
            Picasso.with(getContext()).load(currTweet.getUser().getProfile_image_url()).
                    into(viewHolder.ivProfPic);
        }
        viewHolder.ivProfPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ProfileActivity.class);
                i.putExtra(Constants.userKey, currTweet.getUser());
                getContext().startActivity(i);
            }
        });

        viewHolder.lvMedia.removeAllViews();
        //ArrayList<String> media_urls = currTweet.getMedia_urls();
        //if (media_urls != null && media_urls.size() > 0) {
        //    for (int i = 0; i < media_urls.size(); i++) {
        if (currTweet.getMedia_url() != null) {
            View line = LayoutInflater.from(getContext()).inflate(R.layout.item_photo,
                    parent, false);
            ImageView ivMedia = (ImageView) line.findViewById(R.id.ivMedia);
            ivMedia.setImageResource(0);
            Picasso.with(getContext()).load(currTweet.getMedia_url() + ":small").into(ivMedia);
            viewHolder.lvMedia.addView(line);
        }
        //    }
       // }
        return convertView;
    }

    // To get the date all pretty, like 5 minutes ago, 30 days ago.
    public String getRelativeTimeAgo(String rawJsonDate) {

        String space = " ";
        SimpleDateFormat sf = new SimpleDateFormat(Constants.twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String[] relativeDateParts = relativeDate.split(space);
        relativeDate = relativeDateParts[0];
        if (relativeDateParts.length > 1) {
            relativeDate = relativeDate + relativeDateParts[1].charAt(0);
        }
        return relativeDate;
    }

}
