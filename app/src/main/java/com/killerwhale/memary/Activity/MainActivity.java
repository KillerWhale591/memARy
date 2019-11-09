package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.killerwhale.memary.R;

/**
 * For test only
 */
public class MainActivity extends AppCompatActivity {

    private Button btnSignOut;
    private Button btnSignInActivity;
    private Button btnSignUpActivity;
    private Button btnMapActivity;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mAuth = FirebaseAuth.getInstance();
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnSignUpActivity = (Button) findViewById(R.id.btnSignUpActivity);
        btnSignInActivity = (Button) findViewById(R.id.btnSignInActivity);
        btnMapActivity = (Button) findViewById(R.id.btnMapActivity);

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
        btnMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), MapActivity.class);
                startActivity(i);
            }
        });
    }
}
