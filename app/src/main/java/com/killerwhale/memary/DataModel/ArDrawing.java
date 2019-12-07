package com.killerwhale.memary.DataModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model for all ar drawings
 * @author Zeyu Fu
 */
public class ArDrawing {

    public static final String FIELD_UID = "uid";
    public static final String FIELD_STROKE = "stroke";

    private String mUid;
    private String mStroke;

    /**
     * Empty constructor
     */
    public ArDrawing() { }

    /**
     * Constructor
     * @param uid current user uid
     * @param url stroke file url
     */
    public ArDrawing(String uid, String url) {
        mUid = uid;
        mStroke = url;
    }

    /**
     * Return a map object for write into firebase
     * @return map
     */
    public Map<String, Object> getHashMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_UID, mUid);
        map.put(FIELD_STROKE, mStroke);
        return map;
    }
}
