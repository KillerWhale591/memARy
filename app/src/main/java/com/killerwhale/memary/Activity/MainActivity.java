package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.killerwhale.memary.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import org.w3c.dom.Text;

import java.util.List;

/**
 * For test only
 */
public class MainActivity extends AppCompatActivity implements PermissionsListener {

    private Button btnSignOut;
    private Button btnSignInActivity;
    private Button btnSignUpActivity;

    private Button btnMapActivity;
    private Button btnUserInfoActivity;
    private Button btnProfileActivity;
    private TextView txtUser;
    private PermissionsManager permissionsManager;

    private Button btnARActivity;
    private Button btnARDrawActivity;
    private Button btnARPrimitiveActivity;
    private FirebaseAuth mAuth;
    private Button btnLocationList;
    private Button btnSplash;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            txtUser.setText("User: null");
        } else {
            txtUser.setText("User: " + currentUser.getUid());
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FireBase init.
        mAuth = FirebaseAuth.getInstance();

        // UI init.
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnSignUpActivity = (Button) findViewById(R.id.btnSignUpActivity);
        btnSignInActivity = (Button) findViewById(R.id.btnSignInActivity);

        btnLocationList = (Button) findViewById(R.id.btnLocationList);
        btnMapActivity = (Button) findViewById(R.id.btnMapActivity);
        btnUserInfoActivity = (Button) findViewById(R.id.btnUserInfoActivity);
        txtUser = (TextView) findViewById(R.id.txtUser);
        btnSplash = findViewById(R.id.btnSplash);
        btnProfileActivity = (Button) findViewById(R.id.btnProfileActivity);
        btnARActivity = (Button) findViewById(R.id.btnARActivity);

        btnARActivity = (Button) findViewById(R.id.btnARActivity);

        findViewById(R.id.btnPostFeed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), PostFeedActivity.class));
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "signed out successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), SignInActivity.class);
                startActivity(intent);
            }
        });

        btnSignInActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SignInActivity.class);
                startActivity(intent);
            }
        });

        btnSignUpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnLocationList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), LocationListActivity.class);
                startActivity(i);
            }
        });
        btnMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();

            }
        });
        btnSplash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), SplashActivity.class));
            }
        });

        btnUserInfoActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), UserInfoActivity.class);
                startActivity(intent);
            }
        });

        btnProfileActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });


        btnARActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ARActivity.class);
                startActivity(intent);
            }
        });


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
