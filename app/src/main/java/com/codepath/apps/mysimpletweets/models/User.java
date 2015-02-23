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

    @Column(name = "screen_name")
    private String screen_name;

    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;

    @Column(name = "is_current_user")
    private boolean is_current_user;

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

    public static User fromJson (JSONObject userObject) {
        String userNameKey = "name";
        String userProfileImageUrlKey = "profile_image_url";
        String userIdKey = "id";
        String userScreenNameKey = "screen_name";

        User user = new User();
        try {
            user.name = userObject.getString(userNameKey);
            user.profile_image_url = userObject.getString(userProfileImageUrlKey);
            user.uid = userObject.getLong(userIdKey);
            user.screen_name = userObject.getString(userScreenNameKey);
            //user.save();

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
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(profile_image_url);
        dest.writeString(screen_name);
        dest.writeLong(uid);
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
