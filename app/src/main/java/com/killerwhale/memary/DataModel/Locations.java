package com.killerwhale.memary.DataModel;

public class Locations {
    private long mLocationID;
    private double mLatitude;
    private double mLongtitude;

    public Locations(int Id, double Latitude, Double Longtitude){
        mLocationID = Id;
        mLatitude = Latitude;
        mLongtitude = Longtitude;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public double getmLongtitude() {
        return mLongtitude;
    }

    public long getmLocationID() {
        return mLocationID;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public void setmLocationID(long LocationID) {
        this.mLocationID = LocationID;
    }

    public void setmLongtitude(double mLongtitude) {
        this.mLongtitude = mLongtitude;
    }
    public Locations findlocationById(int id){
        if(this.getmLocationID() == id){
            return this;
        }
        return null;
    }

}
