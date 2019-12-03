package com.killerwhale.memary.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.killerwhale.memary.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText resetEmail;
    private EditText resetPassword;
    private EditText resetConfirmPassword;
    private Button btnReset;
    private Button btnBack;
    private ProgressBar progressReset;

    @Override
    protected void onStart() {
        super.onStart();
        btnBack.setAlpha(1f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        resetEmail = (EditText) findViewById(R.id.resetEmail);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnBack = (Button) findViewById(R.id.btnBack);
        progressReset = (ProgressBar) findViewById(R.id.progressReset);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = resetEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter your registered email", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressReset.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressReset.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBack.setAlpha(0.5f);
                Intent i = new Intent(getBaseContext(), SignInActivity.class);
                startActivity(i);
            }
        });
    }
}
