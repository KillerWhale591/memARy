package com.killerwhale.memary.DataModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

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
    private GeoPoint mLocation;
    private Timestamp mPostTime;

    public Post(int type, String postText, String imageUrl, GeoPoint location, Timestamp postTime) {
        mType = type;
        mPostText = postText;
        mImageUrl = imageUrl;
        mLocation = location;
        mPostTime = postTime;
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
        this.mLocation = location;
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
        return mLocation;
    }

    public Timestamp getPostTime() {
        return mPostTime;
    }

    public Map<String, Object> getHashMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_TYPE, mType);
        map.put(FIELD_TEXT, mPostText);
        map.put(FIELD_IMAGE, mImageUrl);
        map.put(FIELD_LOCATION, mLocation);
        map.put(FIELD_TIMESTAMP, mPostTime);
        return map;
    }

}
