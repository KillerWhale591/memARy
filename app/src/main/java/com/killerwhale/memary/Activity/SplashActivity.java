package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.killerwhale.memary.Helper.PermissionHelper;
import com.killerwhale.memary.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_splash);
        if (!PermissionHelper.hasPermissions(getBaseContext(), PermissionHelper.PERMISSIONS_SPLASH)) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    PermissionHelper.PERMISSIONS_SPLASH,
                    PermissionHelper.PERMISSION_CODE_SPLASH);
        } else {
            enterApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionHelper.PERMISSION_CODE_SPLASH) {
            if (PermissionHelper.hasGrantedAll(grantResults)) {
                checkUser();
            }
        }
    }

    private void enterApp() {
        Handler handler = new Handler();
        Runnable jumpTo = new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        };
        handler.postDelayed(jumpTo, 2000);
    }

    private void checkUser() {
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
}
