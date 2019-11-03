package com.killerwhale.memary.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.killerwhale.memary.R;

public class PostCreateActivity extends AppCompatActivity {

    private static final float ALPHA_CLICKED = 0.5f;

    private Button btnCancel;
    private Button btnSubmit;
    private ImageButton btnAddImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);

        btnCancel = findViewById(R.id.btnCancel);
        btnAddImg = findViewById(R.id.btnAddImg);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAlpha(ALPHA_CLICKED);
                finish();
            }
        });

        btnAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAlpha(ALPHA_CLICKED);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
