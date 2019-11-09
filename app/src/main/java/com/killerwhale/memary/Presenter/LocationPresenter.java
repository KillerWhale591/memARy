package com.killerwhale.memary.Presenter;

import com.killerwhale.memary.DataModel.Locations;

public class LocationPresenter {
    private Locations[] mlocations = new Locations[]{
            new Locations(1000001,42.3602,-71.051),
            new Locations(1000002,42.3603,-71.052),
            new Locations(1000003,42.3604,-71.053),
            new Locations(1000004,42.3605,-71.054),
            new Locations(1000005,42.3606,-71.055),
            new Locations(1000006,42.3607,-71.056),
            new Locations(1000007,42.3608,-71.057),
            new Locations(1000008,42.3609,-71.058),
            new Locations(1000009,42.3610,-71.059)
    };
    public Locations[] getLocations(){
        return mlocations;
    }

}
