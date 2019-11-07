package com.killerwhale.memary.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.killerwhale.memary.R;

public class PostCreateActivity extends AppCompatActivity {

    private static final float ALPHA_CLICKED = 0.5f;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 202;
    private static final int PERMISSION_REQUEST_CAMERA = 1001;

    private Button btnCancel;
    private Button btnSubmit;
    private ImageButton btnAddImg;
    private ImageView imgAttach;
    private ImageButton btnRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);

        btnCancel = findViewById(R.id.btnCancel);
        btnAddImg = findViewById(R.id.btnAddImg);
        btnSubmit = findViewById(R.id.btnSubmit);
        imgAttach = findViewById(R.id.imgAttach);
        btnRemove = findViewById(R.id.btnRemove);

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
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    ActivityCompat.requestPermissions(PostCreateActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSION_REQUEST_CAMERA);
                } else {
                    // Permission has already been granted
                    takePhoto();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showKeyboard();
    }

    @Override
    protected void onPause() {
        hideKeyboard();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                imgAttach.setVisibility(View.VISIBLE);
                btnRemove.setVisibility(View.VISIBLE);
                btnAddImg.setEnabled(false);
                assert data != null;
                Bundle bundleData = data.getExtras();           //images are stored in a bundle wrapped within the intent...
                assert bundleData != null;
                Bitmap Photo = (Bitmap)bundleData.get("data");  //the bundle key is "data".  Requires some reading of documentation to remember. :)
                imgAttach.setImageBitmap(Photo);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            }
        }
    }

    /**
     * Show system keyboard when activity started
     */
    private void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * Hide system keyboard when activity stopped
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /**
     * Start a camera session to take photo
     */
    private void takePhoto() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, REQUEST_CODE_IMAGE_CAPTURE);
    }
}
