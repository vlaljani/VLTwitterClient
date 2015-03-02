package com.codepath.apps.mysimpletweets.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.activities.HomeTimelineActivity;
import com.codepath.apps.mysimpletweets.activities.TweetDetailActivity;
import com.codepath.apps.mysimpletweets.adapters.TweetsAdapter;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.helpers.EndlessScrollListener;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vibhalaljani on 2/26/15.
 */
public abstract class TweetsListFragment extends Fragment {

    private ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<Tweet> tweetArrayList;
    private TweetsAdapter tweetArrayAdapter;
    protected TwitterClient client;

    private static final String TAG = "TWEETLISTFRAGMENT";
    private OnItemSelectedListener listener;

    // Inflation logic
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweets_list, container, false);
        setupViews(view);

        return view;
    }


    // Creation lifecycle event
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApplication.getRestClient();
        tweetArrayList = new ArrayList<>();
        tweetArrayAdapter = new TweetsAdapter(getActivity(), tweetArrayList);
    }

    private void setupViews(View view) {
        lvTweets = (ListView) view.findViewById(R.id.lvTweets);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(R.color.twitter_blue,
                R.color.white,
                R.color.twitter_blue,
                R.color.white);

        lvTweets.setAdapter(tweetArrayAdapter);

        setupViewListeners();
    }

    public abstract void populateTimeline(long max_id);

    public interface OnItemSelectedListener {
        public void onTweetItemSelected(Tweet tweet, User user);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    private void setupViewListeners() {
        /* Algorithm for pagination: If it's the first request, send only count. With every request
         * keep track of the lowest tweet id returned. Use that as the max_id in the next request.
         * For requests after the first, remove the first tweet returned because API requests with
         * max_id are inclusive.*/

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // No point scrolling further if we don't have network
                if (Constants.isNetworkAvailable(getActivity())) {
                    long curr_lowest_max_id = Constants.max_id_first_req;
                    if (tweetArrayList.size() > 0) {
                        Log.i(TAG, "size " + tweetArrayList.size());
                        curr_lowest_max_id = tweetArrayList.get(0).getuid();
                        Log.i(TAG, "what is it here? " + curr_lowest_max_id);
                        for (int i = 1; i < tweetArrayList.size(); i++) {
                            if (tweetArrayList.get(i).getuid() < curr_lowest_max_id) {
                                curr_lowest_max_id = tweetArrayList.get(i).getuid();
                                Log.i(TAG, "now? " + curr_lowest_max_id);
                            }
                        }
                    }
                    Log.i(TAG, "MORE EEE " + curr_lowest_max_id);
                    populateTimeline(curr_lowest_max_id);
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.internet_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listener.onTweetItemSelected(tweetArrayList.get(position),
                                                     tweetArrayList.get(position).getUser());
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // No point refreshing if we don't have data
                if (Constants.isNetworkAvailable(getActivity())) {
                    tweetArrayList.clear();
                    tweetArrayAdapter.notifyDataSetChanged();
                    //new Delete().from(Tweet.class).execute();
                    //new Delete().from(User.class).where("is_current_user = ?", false).execute();

                    populateTimeline(Constants.max_id_first_req);
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.internet_error),
                            Toast.LENGTH_SHORT).show();
                }
                swipeContainer.setRefreshing(false);

            }
        });
    }

    public ArrayList<Tweet> getTweetArrayList() {
        return tweetArrayList;
    }

    public void addAll(List<Tweet> tweets) {
        tweetArrayList.addAll(tweets);
        tweetArrayAdapter.notifyDataSetChanged();
    }

    public void clear() {
        tweetArrayList.clear();
    }


}
