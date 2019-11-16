package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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

    FirebaseFirestore db;

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
        rvAdapter.init();
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
                startActivity(new Intent(getBaseContext(), PostCreateActivity.class));
            }
        });
    }

    @Override
    public void stopRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
