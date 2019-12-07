package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.killerwhale.memary.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.util.List;

public class SplashActivity extends AppCompatActivity implements PermissionsListener {

    private FirebaseAuth mAuth;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_splash);
        checkPermission();
        Handler handler = new Handler();
        Runnable jumpTo = new Runnable() {
            @Override
            public void run() {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Intent i = new Intent(getBaseContext(), MapActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getBaseContext(), SignInActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        handler.postDelayed(jumpTo, 2000);
    }
    public void checkPermission(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Intent i = new Intent(getBaseContext(), MapActivity.class);
            startActivity(i);
        }
        else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getApplicationContext(),"Explaination invalid for now ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            Intent i = new Intent(getBaseContext(), MapActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(),"Permission not Granted yet", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
}
