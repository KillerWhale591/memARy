package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.killerwhale.memary.Presenter.OnRefreshCompleteListener;
import com.killerwhale.memary.Presenter.PostFeedAdapter;
import com.killerwhale.memary.R;

/**
 * Activity for displaying nearby posts
 * @author Zeyu Fu
 */
public class PostFeedActivity extends AppCompatActivity implements OnRefreshCompleteListener {

    private static final long INTERVAL_LOC_REQUEST = 5000;
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    // Location
    private FusedLocationProviderClient FLPC;
    private LocationRequest locationRequest;

    // Firebase
    private FirebaseFirestore db;

    // UI
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView postList;
    PostFeedAdapter rvAdapter;
    RecyclerView.LayoutManager rvManager;
    FloatingActionButton btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        // Database init.
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // UI
        btnCreate = findViewById(R.id.btnCreate);
        postList = findViewById(R.id.postList);
        rvManager = new LinearLayoutManager(this);
        postList.setLayoutManager(rvManager);
        rvAdapter = new PostFeedAdapter(getBaseContext(), db, postList, this);
        postList.setAdapter(rvAdapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                rvAdapter.refreshData();
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            Intent i = new Intent(getBaseContext(), PostCreateActivity.class);
                            i.putExtra(KEY_LATITUDE, location.getLatitude());
                            i.putExtra(KEY_LONGITUDE, location.getLongitude());
                            startActivity(i);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Location
        FLPC = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
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
                    rvAdapter.init(location);
                }
            }
        });
    }

    @Override
    public void stopRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
