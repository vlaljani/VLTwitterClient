package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.mysimpletweets.helpers.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by vibhalaljani on 2/19/15.
 */
@Table(name = "Users")
public class User extends Model implements Parcelable {

    private static final String TAG = "USERCLASS";

    @Column(name = "name")
    private String name;

    private String profile_image_url;
    private String profile_bg_image_url;

    private Boolean profile_protected;

    @Column(name = "screen_name")
    private String screen_name;

    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;

    @Column(name = "is_current_user")
    private boolean is_current_user;

    @Column(name = "followers_count")
    private int followers_count;

    @Column(name = "following_count")
    private int following_count;

    @Column(name = "tweets_count")
    private int tweets_count;

    @Column(name = "description")
    private String description;

    public String getProfile_bg_image_url() {
        return profile_bg_image_url;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public int getFollowing_count() {
        return following_count;
    }

    public int getTweets_count() {
        return tweets_count;
    }

    public String getDescription() {
        return description;
    }

    // Getters and setters where needed
    public String getScreen_name() {
        return screen_name;
    }

    public void setId(long id) {
        this.uid = id;
    }
    public long getUid() { return uid; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setIs_current_user(boolean is_current_user) {
        this.is_current_user = is_current_user;
    }

    public User() {
        super();
        name = null;
        profile_image_url = null;
        screen_name = null;
        is_current_user = false;
        uid = -1;
    }

    public Boolean getProfile_protected() {
        return profile_protected;
    }

    public static User fromJson (JSONObject userObject) {
        String userNameKey = "name";
        String userProfileImageUrlKey = "profile_image_url";
        String userIdKey = "id";
        String userScreenNameKey = "screen_name";
        String followersCountKey = "followers_count";
        String followingCountKey = "friends_count";
        String tweetsCountKey = "statuses_count";
        String profileBgImgUrlKey = "profile_background_image_url";
        String descriptionKey = "description";
        String protectedKey = "protected";


        User user = new User();
        try {
            user.name = userObject.getString(userNameKey);
            user.profile_image_url = userObject.getString(userProfileImageUrlKey);
            user.uid = userObject.getLong(userIdKey);
            user.screen_name = userObject.getString(userScreenNameKey);
            user.profile_bg_image_url = userObject.getString(profileBgImgUrlKey);
            if (user.profile_bg_image_url.indexOf(Constants.defaultBgImgUrl) >= 0) {
                user.profile_bg_image_url = null;
            }
            user.tweets_count = userObject.getInt(tweetsCountKey);
            user.followers_count = userObject.getInt(followersCountKey);
            user.following_count = userObject.getInt(followingCountKey);
            user.description = userObject.getString(descriptionKey);
            user.profile_protected = userObject.getBoolean(protectedKey);

        } catch (JSONException e) {
            Log.e(TAG, Constants.jsonError + " " + e.toString());
        }
        return user;
    }

    private User(Parcel in) {
        this.name = in.readString();
        this.profile_image_url = in.readString();
        this.screen_name = in.readString();
        this.uid = in.readLong();
        this.profile_bg_image_url = in.readString();
        this.tweets_count = in.readInt();
        this.following_count = in.readInt();
        this.followers_count = in.readInt();
        this.profile_protected = in.readByte() != 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(profile_image_url);
        dest.writeString(screen_name);
        dest.writeLong(uid);
        dest.writeString(profile_bg_image_url);
        dest.writeInt(tweets_count);
        dest.writeInt(following_count);
        dest.writeInt(followers_count);
        dest.writeByte((byte) (profile_protected ? 1 : 0));
    }

    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {

        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public List<User> users() {
        return getMany(User.class, "user");
    }

    public static void saveIf(User user) {
        User existingUser =
                new Select().from(User.class).where("uid = ?", user.uid).executeSingle();
        if (existingUser == null) {
            // save user
            user.save();
        }
    }
}
