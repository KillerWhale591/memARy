package com.killerwhale.memary.DataModel;

import android.location.Location;

import com.google.firebase.firestore.GeoPoint;

import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationModel {

    public static final String FIELD_POST = "posts";

    public String location;
    public String address;
    public float distance;
    public int img;
    public int numPosts;
    public ArrayList<String> posts;
    private GeoPoint geoPoint;

    public LocationModel(String location, String address, float distance, int img, int numPosts) {
        this.location = location;
        this.address = address;
        this.distance = distance;
        this.img = img;
        this.numPosts = numPosts;
    }

    public LocationModel(String name, String address, GeoPoint geoPoint, ArrayList<String> posts) {
        this.location = name;
        this.address = address;
        this.posts = posts;
        this.geoPoint = geoPoint;

    }

    public HashMap<String, Object> getLocationMap(){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("address", address);
        hashMap.put("name", location);
        hashMap.put("posts", posts);
        hashMap.put("geopoint", geoPoint);
        return hashMap;
    }

}