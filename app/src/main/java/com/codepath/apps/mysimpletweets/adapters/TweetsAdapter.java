package com.codepath.apps.mysimpletweets.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.squareup.picasso.Picasso;


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

    private static class ViewHolder {
        ImageView ivProfPic;
        TextView tvUserName;
        TextView tvScreenName;
        TextView tvCreatedAt;
        TextView tvTweet;
        LinearLayout lvMedia;
        TextView tvRetweet;
        TextView tvFavorites;
    }
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        super(context, android.R.layout.simple_list_item_1, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tweet currTweet = getItem(position);
        ViewHolder viewHolder;
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvUserName.setText(currTweet.getUser().getName());
        viewHolder.tvScreenName.setText(" " + Constants.twitterUserRef +
                                                    currTweet.getUser().getScreen_name());
        viewHolder.tvCreatedAt.setText(getRelativeTimeAgo(currTweet.getCreated_at()));
        viewHolder.tvTweet.setText(currTweet.getText());

        if (currTweet.getRetweet_count() > 0) {
            viewHolder.tvRetweet.setText(String.valueOf(currTweet.getRetweet_count()));
        } else {
            viewHolder.tvRetweet.setText("");
        }

        if (currTweet.getFavorites_count() > 0) {
            viewHolder.tvFavorites.setText(String.valueOf(currTweet.getFavorites_count()));
        } else {
            viewHolder.tvRetweet.setText("");
        }

        viewHolder.ivProfPic.setImageResource(0);
        Picasso.with(getContext()).load(currTweet.getUser().getProfile_image_url()).
                into(viewHolder.ivProfPic);

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
        relativeDate = relativeDateParts[0] + relativeDateParts[1].charAt(0);
        return relativeDate;
    }

}
