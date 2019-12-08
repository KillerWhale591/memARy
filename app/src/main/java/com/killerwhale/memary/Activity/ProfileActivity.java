package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.killerwhale.memary.R;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView navBar;
    private static String TAG = "PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        navBar = findViewById(R.id.navBar);
        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_map:
                        startActivity(new Intent(getBaseContext(), MapActivity.class));
                        finish();
                        break;
                    case R.id.action_posts:
                        startActivity(new Intent(getBaseContext(), PostFeedActivity.class));
                        break;
                    case R.id.action_places:
                        startActivity(new Intent(getBaseContext(), LocationListActivity.class));
                        finish();
                        break;
                    case R.id.action_profile:
                        break;
                    default:
                        Log.i(TAG, "Unhandled nav click");

                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navBar.setSelectedItemId(R.id.action_profile);

    }
}
