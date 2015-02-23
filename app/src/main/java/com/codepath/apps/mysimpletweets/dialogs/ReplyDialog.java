package com.codepath.apps.mysimpletweets.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.helpers.Constants;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vibhalaljani on 2/22/15.
 */
public class ReplyDialog extends DialogFragment {
    private ImageButton btnBack;
    private ImageView ivProfPic;
    private TextView tvUserName;
    private TextView tvScreenName;
    private TextView tvCharCount;
    private Button btnTweet;
    private EditText etNewTweet;
    private long curr_status_uid;

    public ReplyDialog() {

    }

    public static ReplyDialog newInstance(User auth_user, String screen_name, long curr_status_uid) {
        ReplyDialog frag = new ReplyDialog();
        Bundle args = new Bundle();
        args.putParcelable(Constants.authUserKey, auth_user);
        args.putString("screen_name", screen_name);
        args.putLong("curr_status_uid", curr_status_uid);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_compose_tweet, container);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        setupViews(view);

        return view;
    }

    private void setupViews(View view) {
        User auth_user = getArguments().getParcelable(Constants.authUserKey);
        String screen_name = getArguments().getString("screen_name");
        curr_status_uid = getArguments().getLong("curr_status_uid");

        btnBack = (ImageButton) view.findViewById(R.id.btnBack);
        ivProfPic = (ImageView) view.findViewById(R.id.ivProfPic);
        tvUserName = (TextView) view.findViewById(R.id.tvUserName);
        tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);
        tvCharCount = (TextView) view.findViewById(R.id.tvCharCount);
        etNewTweet = (EditText) view.findViewById(R.id.etNewTweet);
        btnTweet = (Button) view.findViewById(R.id.btnTweet);

        ivProfPic.setImageResource(0);
        Picasso.with(getActivity()).load(auth_user.getProfile_image_url()).into(ivProfPic);
        tvUserName.setText(auth_user.getName());
        tvScreenName.setText(Constants.twitterUserRef + auth_user.getScreen_name());
        tvCharCount.setText(String.valueOf(getResources().getInteger(R.integer.start_char_count)));

        setupViewListeners();

        etNewTweet.append(Constants.twitterUserRef + screen_name + " ");
    }

    private void setupViewListeners() {

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        etNewTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnTweet.setEnabled(true);
                    btnTweet.setAlpha(1);
                } else if (s.length() <= 0 || s.length() > getResources().
                        getInteger(R.integer.start_char_count)) {
                    btnTweet.setEnabled(false);
                    btnTweet.setAlpha((float)0.60);
                }
                tvCharCount.setText(String.valueOf(getResources().getInteger(R.integer.start_char_count) -
                        s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ReplyDialogListener listener = (ReplyDialogListener) getActivity();

                final Tweet newTweet = new Tweet();
                newTweet.setText(etNewTweet.getText().toString());
                User user = getArguments().getParcelable(Constants.userKey);
                newTweet.setUser(user);
                newTweet.setCreated_at(getCurrentTimeStamp());

                TwitterClient client = TwitterApplication.getRestClient();
                client.reply(etNewTweet.getText().toString(), curr_status_uid,
                        new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                        if (jsonObject != null) {
                            listener.onFinishReplyDialog(newTweet);
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.sth_wrong),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers,
                                          Throwable t,
                                          JSONObject e) {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.internet_error),
                                Toast.LENGTH_SHORT).show();
                        Log.i("COMPOSE", Constants.jsonError + " Throwable: " + t.toString() +
                                " JSONObject: " + e.toString());
                    }
                });
            }
        });
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat(Constants.twitterFormat);//dd/MM/yyyy
        Date now = new Date();
        return sdfDate.format(now);
    }

    public interface ReplyDialogListener {
        void onFinishReplyDialog(Tweet newTweet);
    }
}

