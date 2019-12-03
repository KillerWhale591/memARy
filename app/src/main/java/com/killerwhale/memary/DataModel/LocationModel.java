package com.killerwhale.memary.DataModel;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class LocationModel {

    public String location;
    public String address;
    public float distance;
    public int img;
    private String image;
    public int numPosts;
    private GeoPoint geoPoint;
    private static final float METERS_TO_MILES = 1609.3f;
    private static final float MINIMUM_DISTANCE = 0.1f;
    private static final String SUFFIX_MILES = " miles away";

    public LocationModel(String location, String address, float distance, int img, int numPosts) {
        this.location = location;
        this.address = address;
        this.distance = distance;
        this.img = img;
        this.numPosts = numPosts;
    }

    public LocationModel(Map<String, Object> map){
        HashMap LocationData = (HashMap) map;
        this.address = (String) LocationData.get("address");
        this.geoPoint = (GeoPoint) LocationData.get("geopoint");
        this.image = (String) LocationData.get("image");
        this.location = (String) LocationData.get("name");
    }

    public String getLocation() {
        return location;
    }

    public String getAddress() {
        return address;
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
}