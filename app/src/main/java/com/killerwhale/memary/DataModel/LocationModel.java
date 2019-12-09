package com.killerwhale.memary.DataModel;

import android.location.Location;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocationModel {
    public static final String FIELD_POST = "posts";
    public static final String FIELD_NUMPOSTS = "numPosts";
    public String location;
    public String address;
    public float distance;
    public int numPosts;
    public ArrayList<String> posts;
    private GeoPoint geoPoint;
    private static final float METERS_TO_MILES = 1609.3f;
    private static final float MINIMUM_DISTANCE = 0.1f;
    private static final String SUFFIX_MILES = " miles away";


    public LocationModel(String name, String address, GeoPoint geoPoint, ArrayList<String> posts, int numPosts) {
        this.location = name;
        this.address = address;
        this.posts = posts;
        this.geoPoint = geoPoint;
        this.numPosts = numPosts;
    }


    public LocationModel(Map<String, Object> map){
        HashMap LocationData = (HashMap) map;
        this.address = (String) LocationData.get("address");
        this.geoPoint = (GeoPoint) LocationData.get("geopoint");
        this.location = (String) LocationData.get("name");
        if (LocationData.get("posts") == null) this.numPosts = 0;
        else this.numPosts = ((List) Objects.requireNonNull(LocationData.get("posts"))).size();
    }


    public HashMap<String, Object> getLocationMap(){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("address", address);
        hashMap.put("name", location);
        hashMap.put("posts", posts);
        hashMap.put("geopoint", geoPoint);
        hashMap.put("numPosts", numPosts);
        return hashMap;
    }


    public String getLocation() {
        return location;
    }


    public String getAddress() {
        return address;
    }


    public String getPosts() {
        return String.valueOf(numPosts);
    }


    /**
     * Get the distance between a location and the post
     * @param location current location
     * @return distance in string format
     */
    public String getDistance(Location location) {
        Location mLocation = new Location("");
        mLocation.setLatitude(geoPoint.getLatitude());
        mLocation.setLongitude(geoPoint.getLongitude());
        DecimalFormat f = new DecimalFormat("#.#");
        float dist = mLocation.distanceTo(location) / METERS_TO_MILES;
        if (dist < MINIMUM_DISTANCE) {
            dist = MINIMUM_DISTANCE;
        }
        return Float.valueOf(f.format(dist)) + SUFFIX_MILES;
    }


    public GeoPoint getGeoPoint() {
        return geoPoint;
    }
}