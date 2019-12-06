package com.killerwhale.memary.DataModel;

public class LocationModel {

    public String location;
    public String address;
    public float distance;
    public int img;
    public int numPosts;

    public LocationModel(String location, String address, float distance, int img, int numPosts) {
        this.location = location;
        this.address = address;
        this.distance = distance;
        this.img = img;
        this.numPosts = numPosts;
    }

}