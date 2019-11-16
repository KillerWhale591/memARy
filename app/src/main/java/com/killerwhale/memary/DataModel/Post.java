package com.killerwhale.memary.DataModel;

import android.location.Location;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

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

    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_IMAGE = "image";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_TIMESTAMP = "timestamp";

    private String mPostId;
    private int mType;
    private String mPostText;
    private String mImageUrl;
    private GeoPoint mGeoPoint;
    private Timestamp mPostTime;

    /**
     * Constructor of a Post
     * @param type post type: pure text or image post
     * @param postText text of post
     * @param imageUrl image url, default is empty string
     * @param location post location
     * @param postTime post time
     */
    public Post(int type, String postText, String imageUrl, GeoPoint location, Timestamp postTime) {
        mType = type;
        mPostText = postText;
        mImageUrl = imageUrl;
        mGeoPoint = location;
        mPostTime = postTime;
    }

    public Post(Map<String, Object> map) {
        HashMap postData = (HashMap) map;
        mType = ((Long) postData.get(FIELD_TYPE)).intValue();
        mPostText = (String) postData.get(FIELD_TEXT);
        mImageUrl = (String) postData.get(FIELD_IMAGE);
        mGeoPoint = (GeoPoint) postData.get(FIELD_LOCATION);
        mPostTime = (Timestamp) postData.get(FIELD_TIMESTAMP);
    }

    public void setPostId(String postId) {
        this.mPostId = postId;
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

    public String getPostId() {
        return mPostId;
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

    /**
     * Transfer from Post to HashMap
     * @return A HashMap format of post that can be directly submit to database
     */
    public Map<String, Object> getHashMap() {
        Map<String, Object> map = new HashMap<>();
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
     * @return distance
     */
    public double getDistance(Location location) {
        Location mLocation = new Location("");
        mLocation.setLatitude(mGeoPoint.getLatitude());
        mLocation.setLongitude(mGeoPoint.getLongitude());
        return mLocation.distanceTo(location);
    }

    /**
     * Get the post time from now
     * @param curr current timestamp
     * @return time from now in String format
     */
    public String getTimeFromNow(Date curr) {
        long diff = curr.getTime() - mPostTime.toDate().getTime();
        long seconds = diff / 1000;
        int days = (int) seconds / 86400;
        int hours = ((int) seconds % 86400) / 3600;
        int minutes = ((int) seconds % 3600) / 60;
        return buildTimeString(days, hours, minutes);
    }

    private String buildTimeString(int days, int hours, int minutes) {
        if (days > 0) {
            return days + " days ago";
        }
        if (hours > 0) {
            return hours + " hours ago";
        }
        if (minutes > 0) {
            return minutes + " minutes ago";
        }
        return "1 minute ago";
    }
}
