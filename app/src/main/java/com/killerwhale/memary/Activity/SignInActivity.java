package com.killerwhale.memary.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Activity for login with email
 * @author Haoxuan Jia
 */
public class SignInActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText loginEmail;
    private EditText loginPassword;
    private Button btnLogin;
    private Button btnGoToReset;
    private Button btnGoToRegister;
    private SignInButton btnGoogleLogin;
    private ProgressBar progressLogin;
    private GoogleSignInClient mGoogleSignInClient;
    private StorageReference storageRef;

    private static final int RC_SIGN_IN = 1111;
    private static final String TAG = "BLUESOX";

    @Override
    protected void onStart() {
        super.onStart();
        btnGoToReset.setAlpha(1f);
        btnGoToRegister.setAlpha(1f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("avatars");

        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGoToReset = (Button) findViewById(R.id.btnGoToReset);
        btnGoToRegister = (Button) findViewById(R.id.btnGoToRegister);
        progressLogin = (ProgressBar) findViewById(R.id.progressLogin);

        btnGoogleLogin = (SignInButton) findViewById(R.id.btnGoogleLogin);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_login_token))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

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

                progressLogin.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressLogin.setVisibility(View.INVISIBLE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    Toast.makeText(SignInActivity.this, "sign in failed", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SignInActivity.this, "signed in successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignInActivity.this, MapActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });

        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGoToRegister.setAlpha(0.5f);
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnGoToReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGoToReset.setAlpha(0.5f);
                Intent intent = new Intent(getBaseContext(), ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.i(TAG, "Google sign in suceessfully");
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.i(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.i(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final String uid = mAuth.getCurrentUser().getUid();
                            Log.i(TAG, uid);
                            DocumentReference docRef = db.collection("users").document(uid);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.getResult() == null || !task.getResult().exists()){
                                        startActivity(new Intent(getBaseContext(), UserInfoActivity.class));
                                    } else{
                                        startActivity(new Intent(getBaseContext(), MapActivity.class));
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "google sign in failed", Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }
}
