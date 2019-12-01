package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.killerwhale.memary.R;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_splash);
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
}
