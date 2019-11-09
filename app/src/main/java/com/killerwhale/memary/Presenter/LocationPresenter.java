package com.killerwhale.memary.Presenter;

import com.killerwhale.memary.DataModel.Locations;

public class LocationPresenter {
    private Locations[] mlocations = new Locations[100];
    public LocationPresenter(){
        double lat = 42.2654;
        double long1 = -71.1606;
        for( int i = 0 ; i < mlocations.length; i++){
            mlocations[i] = new Locations(i,lat, long1);
            lat-= 0.003;
            long1 -= 0.003;
        }
    }

    public Locations[] getLocations(){
        return mlocations;
    }

}
