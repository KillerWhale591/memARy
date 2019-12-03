package com.killerwhale.memary.Activity;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.killerwhale.memary.Presenter.LocationListAdapter;
import com.killerwhale.memary.R;

public class LocationListActivity extends AppCompatActivity {

    private static final long INTERVAL_LOC_REQUEST = 5000;

    private ListView locationList;
    private FirebaseFirestore db;

    private LocationListAdapter llAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        locationList = (ListView) findViewById(R.id.locationList);
        llAdapter = new LocationListAdapter(this.getBaseContext());
        locationList.setAdapter(llAdapter);

        // Database init.
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Location
        // Location
        FusedLocationProviderClient FLPC = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL_LOC_REQUEST);
        // Get post location
        FLPC.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        }, null);
        FLPC.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    llAdapter.getLocation(location);
                }
            }
        });
        llAdapter.init();
        llAdapter.queryByName(llAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mnu_distance:
//                llAdapter.sortByDistance();
                llAdapter.queryByDistance(llAdapter, 0);
                break;
            case R.id.mnu_name:
//                llAdapter.sortByName();
                llAdapter.queryByName(llAdapter);
                break;
            case R.id.mnu_posts:
//                llAdapter.sortByPost();
                llAdapter.queryByPosts(llAdapter);
                break;
        }
        locationList.setAdapter(llAdapter);
        return true;
    }
}