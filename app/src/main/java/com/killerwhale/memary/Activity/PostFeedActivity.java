package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.killerwhale.memary.Presenter.OnRefreshCompleteListener;
import com.killerwhale.memary.Presenter.PostFeedAdapter;
import com.killerwhale.memary.R;

/**
 * Activity for displaying nearby posts
 * @author Zeyu Fu
 */
public class PostFeedActivity extends AppCompatActivity implements OnRefreshCompleteListener {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView postList;
    RecyclerView.Adapter rvAdapter;
    RecyclerView.LayoutManager rvManager;
    FloatingActionButton btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        btnCreate = findViewById(R.id.btnCreate);
        postList = findViewById(R.id.postList);
        rvManager = new LinearLayoutManager(this);
        postList.setLayoutManager(rvManager);
        rvAdapter = new PostFeedAdapter(getBaseContext(), postList, this);
        postList.setAdapter(rvAdapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((PostFeedAdapter)rvAdapter).refreshData();
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void stopRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
