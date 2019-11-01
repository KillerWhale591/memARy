package com.killerwhale.memary.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.killerwhale.memary.R;

public class PostFeedActivity extends AppCompatActivity {

    RecyclerView postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        postList = findViewById(R.id.postList);
    }
}
