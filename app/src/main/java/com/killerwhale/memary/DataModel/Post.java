package com.killerwhale.memary.DataModel;

import android.location.Location;

/**
 * Data model for posts
 * @author Zeyu Fu
 */
public class Post {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;

    private long mPostId;
    private int mType;
    private String mPostText;
    private String mImageUrl;
    private long mLocationId;

    public Post(long postId, int type, String postText, String imageUrl, long locationId) {
        mPostId = postId;
        mType = type;
        mPostText = postText;
        mImageUrl = imageUrl;
        mLocationId = locationId;
    }

    public void setPostId(long postId) {
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

    public void setLocationId(long locationId) {
        this.mLocationId = locationId;
    }

    public long getPostId() {
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

    public long getLocationId() {
        return mLocationId;
    }


}
