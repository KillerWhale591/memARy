package com.killerwhale.memary.DataModel;

import android.location.Location;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Data model for posts
 * @author Zeyu Fu
 */
public class Post {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AR = 2;

    private static final String FIELD_UID = "uid";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_IMAGE = "image";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String SUFFIX_MILES = " miles away";
    private static final String SUFFIX_DAYS = " days ago";
    private static final String SUFFIX_HOURS = " hours ago";
    private static final String SUFFIX_MINUTES = " minutes ago";
    private static final String ONE_MINUTE_AGO = "1 minute ago";

    private static final long MSEC_PER_SEC = 1000L;
    private static final int SEC_PER_DAY = 86400;
    private static final int SEC_PER_HOUR = 3600;
    private static final int SEC_PER_MIN = 60;
    private static final float METERS_TO_MILES = 1609.3f;
    private static final float MINIMUM_DISTANCE = 0.1f;

    private String mUid;
    private String mUsername;
    private String mAvatar;
    private int mType;
    private String mPostText;
    private String mImageUrl;
    private GeoPoint mGeoPoint;
    private Timestamp mPostTime;
    private String PostId;

    /**
     * Constructor of a Post
     * @param type post type: pure text or image post
     * @param postText text of post
     * @param imageUrl image url, default is empty string
     * @param location post location
     * @param postTime post time
     */
    public Post(String uid, int type, String postText, String imageUrl, GeoPoint location, Timestamp postTime) {
        mUid = uid;
        mType = type;
        mPostText = postText;
        mImageUrl = imageUrl;
        mGeoPoint = location;
        mPostTime = postTime;
    }

    /**
     * Constructor for a post
     * @param map data from database in HashMap form
     */
    public Post(Map<String, Object> map) {
        HashMap postData = (HashMap) map;
        mUid = (String) postData.get(FIELD_UID);
        mType = ((Long) postData.get(FIELD_TYPE)).intValue();
        mPostText = (String) postData.get(FIELD_TEXT);
        mImageUrl = (String) postData.get(FIELD_IMAGE);
        mGeoPoint = (GeoPoint) postData.get(FIELD_LOCATION);
        mPostTime = (Timestamp) postData.get(FIELD_TIMESTAMP);
    }

    public void setPostId(String postId){
        this.PostId = postId;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setPostText(String postText) {
        this.mPostText = postText;
    }

    public void setImageUrl(String url) {
        this.mImageUrl = url;
    }

    public void setLocation(GeoPoint location) {
        this.mGeoPoint = location;
    }

    public void setUsername(String username) {
        if (username != null) {
            this.mUsername = username;
        }
    }

    public void setAvatar(String avatar) {
        if (avatar != null) {
            this.mAvatar = avatar;
        } else {
            this.mAvatar = "";
        }
    }

    public String getUid() {
        return mUid;
    }

    public int getType() {
        return mType;
    }

    public String getPostText() {
        return mPostText;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public GeoPoint getLocation() {
        return mGeoPoint;
    }

    public Timestamp getPostTime() {
        return mPostTime;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public String getPostId(){return PostId;}

    /**
     * Transfer from Post to HashMap
     * @return A HashMap format of post that can be directly submit to database
     */
    public Map<String, Object> getHashMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_UID, mUid);
        map.put(FIELD_TYPE, mType);
        map.put(FIELD_TEXT, mPostText);
        map.put(FIELD_IMAGE, mImageUrl);
        map.put(FIELD_LOCATION, mGeoPoint);
        map.put(FIELD_TIMESTAMP, mPostTime);
        return map;
    }

    /**
     * Get the distance between a location and the post
     * @param location current location
     * @return distance in string format
     */
    public String getDistance(Location location) {
        Location mLocation = new Location("");
        mLocation.setLatitude(mGeoPoint.getLatitude());
        mLocation.setLongitude(mGeoPoint.getLongitude());
        DecimalFormat f = new DecimalFormat("#.#");
        float dist = mLocation.distanceTo(location) / METERS_TO_MILES;
        if (dist < MINIMUM_DISTANCE) {
            dist = MINIMUM_DISTANCE;
        }
        return Float.valueOf(f.format(dist)) + SUFFIX_MILES;
    }

    /**
     * Get the post time from now
     * @param curr current timestamp
     * @return time from now in String format
     */
    public String getTimeFromNow(Date curr) {
        long diff = curr.getTime() - mPostTime.toDate().getTime();
        long seconds = diff / MSEC_PER_SEC;
        int days = (int) seconds / SEC_PER_DAY;
        int hours = ((int) seconds % SEC_PER_DAY) / SEC_PER_HOUR;
        int minutes = ((int) seconds % SEC_PER_HOUR) / SEC_PER_MIN;
        return buildTimeString(days, hours, minutes);
    }

    /**
     * Build the time string for displaying
     * @param days days
     * @param hours hours
     * @param minutes minutes
     * @return string
     */
    private String buildTimeString(int days, int hours, int minutes) {
        if (days > 0) {
            return days + SUFFIX_DAYS;
        }
        if (hours > 0) {
            return hours + SUFFIX_HOURS;
        }
        if (minutes > 1) {
            return minutes + SUFFIX_MINUTES;
        }
        return ONE_MINUTE_AGO;
    }
}
