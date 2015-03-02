package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.mysimpletweets.helpers.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.activeandroid.annotation.Column.ForeignKeyAction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Created by vibhalaljani on 2/17/15.
 *
 * Parse the JSON + store the data. Encapsulate state/display logic.
 */
@Table(name = "Tweets")

public class Tweet extends Model implements Parcelable {

    // Tag to mark logs

    public void setUser(User user) {
        this.user = user;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    private static final String TAG = "TWEETCLASS";

    @Column(name = "Text")
    private String text;

    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid; // Unique DB ID for the Tweet

    @Column(name = "user", onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    private User user;

    @Column(name = "created_at")
    private String created_at;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "retweet_count")
    private int retweet_count;

    @Column(name = "favorites_count")
    private int favorites_count;

    @Column(name = "favorited")
    private boolean favorited;

    @Column(name = "retweeted")
    private boolean retweeted;

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    //private ArrayList<String> media_urls;
    private String media_url;

    public String getMedia_url() {
        return media_url;
    }

    /*public ArrayList<String> getMedia_urls() {
        return media_urls;
    }*/

    // Getters and setters as needed
    public String getText() {
        return text;
    }

    public long getuid() {
        return uid;
    }

    public void setId(long id) {
        this.uid = id;
    }

    public User getUser() {
        return user;
    }

    public String getCreated_at() {
        return created_at;
    }

    public Tweet() {
        super();
    }




    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweetArrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            // Deserialize JSON and load it into model
            try {
                Tweet tweet = Tweet.fromJSON(jsonArray.getJSONObject(i));

                // Populate list with tweet
                if (tweet != null) {
                    tweetArrayList.add(tweet);
                }
            } catch (JSONException e) {
                Log.e(TAG, Constants.jsonError + " Exception: " + e.toString());
                // Don't want to stop if 1 tweet fails to reach us
                continue;
            }
        }
        return tweetArrayList;
    }

    public int getRetweet_count() {
        return retweet_count;
    }

    public int getFavorites_count() {
        return favorites_count;
    }

    // Deserialize the JSON
    // Tweet.fromJSON(JSONObject) => Tweet
    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        String tweetTextKey = "text";
        String tweetCreatedAtKey = "created_at";
        String tweetUidKey = "id";
        String tweetUserKey = "user";
        String tweetEntitiesKey = "entities";
        String mediaKey = "media";
        String media_urlKey = "media_url";
        String favCountKey = "favorite_count";
        String retweetCountKey = "retweet_count";
        String favoritedKey = "favorited";
        String retweetedKey = "retweeted";

        // Extract values from JSON
        try {

            tweet.text = jsonObject.getString(tweetTextKey);
            tweet.created_at = jsonObject.getString(tweetCreatedAtKey);
            tweet.uid = jsonObject.getLong(tweetUidKey);
            tweet.user = User.fromJson(jsonObject.getJSONObject(tweetUserKey));
            tweet.timestamp = setDateFromString(tweet.created_at);
            tweet.retweet_count = jsonObject.getInt(retweetCountKey);
            tweet.favorites_count = jsonObject.getInt(favCountKey);
            tweet.favorited = jsonObject.getBoolean(favoritedKey);
            tweet.retweeted = jsonObject.getBoolean(retweetedKey);

            //tweet.media_urls = new ArrayList<>();
            tweet.media_url = null;
            // get media
            if (jsonObject.optJSONObject(tweetEntitiesKey) != null) {
                JSONObject entitiesObj = jsonObject.getJSONObject(tweetEntitiesKey);
                if (entitiesObj.optJSONArray(mediaKey) != null) {
                    JSONArray mediaArray = entitiesObj.getJSONArray(mediaKey);
                    if (mediaArray.length() > 0) {
                        tweet.media_url = mediaArray.getJSONObject(0).getString(media_urlKey);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Constants.jsonError + " Exception:  " + e.toString());
        }

        // Store them
        return tweet;
    }

    public static List<Tweet> getAll() {
        return new Select()
                .from(Tweet.class)
                .orderBy("timestamp DESC")
                .execute();
    }

    public static void saveIf(Tweet tweet) {
        Tweet existingTweet =
                new Select().from(Tweet.class).where("uid = ?", tweet.uid).executeSingle();
        if (existingTweet == null) {
            // save tweet
            tweet.save();
        }
    }

    public static Date setDateFromString(String date) {
        SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        sf.setLenient(true);
        Date timestamp = null;
        try {
            timestamp = sf.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }
        return timestamp;
    }

    private Tweet(Parcel in) {
        this.text = in.readString();
        this.uid = in.readLong();
        this.created_at = in.readString();
        this.retweet_count = in.readInt();
        this.favorites_count = in.readInt();
        this.media_url = in.readString();
        this.favorited = in.readByte() != 0;
        this.retweeted = in.readByte() != 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeLong(uid);
        dest.writeString(created_at);
        dest.writeInt(retweet_count);
        dest.writeInt(favorites_count);
        dest.writeString(media_url);
        dest.writeByte((byte) (favorited ? 1 : 0));
        dest.writeByte((byte) (retweeted ? 1 : 0));
    }

    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<Tweet> CREATOR
            = new Parcelable.Creator<Tweet>() {

        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };

    public void setRetweet_count(int retweet_count) {
        this.retweet_count = retweet_count;
    }

    public void setFavorites_count(int favorites_count) {
        this.favorites_count = favorites_count;
    }
}
