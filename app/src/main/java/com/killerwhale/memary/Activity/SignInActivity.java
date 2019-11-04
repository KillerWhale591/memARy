package com.killerwhale.memary.Activity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.killerwhale.memary.R;

/**
 * Activity for login with email
 * @author Haoxuan Jia
 */
public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText loginEmail;
    private EditText loginPassword;
    private Button btnLogin;
    private Button btnGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGoToRegister= (Button) findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    Toast.makeText(SignInActivity.this, "sign in failed", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SignInActivity.this, "signed in successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }

                            }
                        });
            }
        });

        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}
