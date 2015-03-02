package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.adapters.TweetsAdapter;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by vibhalaljani on 2/27/15.
 */
public class UserHeaderFragment extends Fragment {
    private ImageView ivProfileBg;
    private ImageView ivProfPic;
    private TextView tvUserName;
    private TextView tvScreenName;
    private TextView tvTweetCount;
    private TextView tvFollowingCount;
    private TextView tvFollowersCount;

    private static final String TAG = "USERHEADERFRAGMENT";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_header, container, false);
        setupViews(view);

        return view;
    }

    // Creation lifecycle event
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static UserHeaderFragment newInstance(User user) {
        UserHeaderFragment userHeaderFragment = new UserHeaderFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.userKey, user);
        userHeaderFragment.setArguments(args);
        return userHeaderFragment;
    }

    private void setupViews(View view) {
        ivProfileBg = (ImageView) view.findViewById(R.id.ivProfileBg);
        ivProfPic = (ImageView) view.findViewById(R.id.ivProfPic);
        tvUserName = (TextView) view.findViewById(R.id.tvUserName);
        tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);
        tvTweetCount = (TextView) view.findViewById(R.id.tvTweetCount);
        tvFollowingCount = (TextView) view.findViewById(R.id.tvFollowingCount);
        tvFollowersCount = (TextView) view.findViewById(R.id.tvFollowersCount);

        User user = getArguments().getParcelable(Constants.userKey);
        ivProfileBg.setImageResource(0);
        if (user.getProfile_bg_image_url() != null) {
            Picasso.with(getActivity()).load(user.getProfile_bg_image_url()).into(ivProfileBg);
        }
        ivProfPic.setImageResource(0);
        if (user.getProfile_image_url() != null) {
            Picasso.with(getActivity()).load(user.getProfile_image_url()).into(ivProfPic);
        }

        tvUserName.setText(user.getName());
        if (user.getProfile_protected()) {
            tvUserName.setCompoundDrawables(null,
                    null,getResources().getDrawable(R.drawable.ic_lock),null);
        }
        tvScreenName.setText(Constants.twitterUserRef + user.getScreen_name());
        tvTweetCount.setText(String.valueOf(user.getTweets_count()));
        tvFollowingCount.setText(String.valueOf(user.getFollowing_count()));
        tvFollowersCount.setText(String.valueOf(user.getFollowers_count()));
    }
}
