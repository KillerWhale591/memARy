package com.killerwhale.memary.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.killerwhale.memary.Presenter.PostFeedAdapter;
import com.killerwhale.memary.R;

public class PostFeedActivity extends AppCompatActivity {

    RecyclerView postList;
    RecyclerView.Adapter rvAdapter;
    RecyclerView.LayoutManager rvManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        postList = findViewById(R.id.postList);
        rvManager = new LinearLayoutManager(this);
        rvAdapter = new PostFeedAdapter(getBaseContext());
        postList.setLayoutManager(rvManager);
        postList.setAdapter(rvAdapter);
    }
}
