package com.killerwhale.memary.DataModel;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model for all ar drawings
 * @author Zeyu Fu
 */
public class ArDrawing {

    public static final String FIELD_UID = "uid";
    public static final String FIELD_STROKE = "stroke";
    public static final String FIELD_TIME = "timestamp";

    private String mUid;
    private String mStroke;
    private Timestamp mTime;

    /**
     * Empty constructor
     */
    public ArDrawing() { }

    /**
     * Constructor
     * @param uid current user uid
     * @param url stroke file url
     */
    public ArDrawing(String uid, String url, Timestamp timestamp) {
        mUid = uid;
        mStroke = url;
        mTime = timestamp;
    }

    /**
     * Return a map object for write into firebase
     * @return map
     */
    public Map<String, Object> getHashMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_UID, mUid);
        map.put(FIELD_STROKE, mStroke);
        map.put(FIELD_TIME, mTime);
        return map;
    }
}
