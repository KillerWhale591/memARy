package com.killerwhale.memary.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.R;
import com.killerwhale.memary.View.EditUsernameDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements EditUsernameDialog.EditUsernameDialogListener {

    private BottomNavigationView navBar;
    private static String TAG = "PROFILE";
    private FirebaseFirestore db;
    private SimpleDraweeView icUserInfoAvatar;
    private ImageButton btnCamera;
    private ImageButton btnWrite;
    private TextView txtName;
    private Button btnSetting;
    private FirebaseAuth mAuth;
//    private Uri uri = null;
    private StorageReference storageRef;
//    private String remoteUrl = "";
    private Button btnMyPosts;
    private Button btnLogout;

    private static final int PICK_FROM_GALLERY = 9999;
    private String Uid;

    @Override
    protected void onStart() {
        super.onStart();

        // if signed in, get Firebase Auth Uid, else do something
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
//            startActivity(new Intent(getBaseContext(), SignInActivity.class));
        } else {
            Uid = currentUser.getUid();
            if(db != null) {
                final DocumentReference docRef = db.collection("users").document(Uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                String avatarString = (String) document.get("avatar");
                                if (avatarString != null) {
                                    icUserInfoAvatar.setImageURI(Uri.parse(avatarString));
                                }
                                String usernameString = (String) document.get("username");
                                if (usernameString != null) {
                                    txtName.setText(usernameString);
                                }
                            } else {
                                Log.d(TAG, "error getting user profile");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        }
    }

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
                        finish();
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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("avatars");

        icUserInfoAvatar = (SimpleDraweeView) findViewById(R.id.icUserInfoAvatar);
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        txtName = (TextView) findViewById(R.id.txtName);
        btnSetting = (Button) findViewById(R.id.btnSetting);
        btnWrite = (ImageButton) findViewById(R.id.btnWrite);
        btnMyPosts = (Button) findViewById(R.id.btnMyPosts);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if gallery permission not granted, request permission, else go to gallery
                if (ActivityCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                }
            }
        });

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        btnMyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), MyPostsActivity.class));
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), SettingActivity.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(getBaseContext(), "signed out successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void uploadAvatar(Uri uri){
        final StorageReference avatarImgRef = storageRef.child(Uid + ".jpg");
        UploadTask uploadTask = avatarImgRef.putFile(uri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return avatarImgRef.getDownloadUrl();
            }
        });
        urlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        updateUsersAvatar(downloadUri.toString());
                    }
                } else {
                    // Handle failures
                    Log.e(TAG, "Upload failed.");
                }
            }
        });
    }

    private void updateUsersAvatar(String remoteUrl){
        if(db != null) {
            DocumentReference user = db.collection("users").document(Uid);
            user.update("avatar", remoteUrl);
        }
        icUserInfoAvatar.setImageURI(remoteUrl);
    }

    private void updateUsersUsername(String username){
        if(db != null) {
            DocumentReference user = db.collection("users").document(Uid);
            user.update("username", username);
        }
        txtName.setText(username);
    }

    private void openDialog(){
        EditUsernameDialog editUsernameDialog = new EditUsernameDialog();
        editUsernameDialog.show(getSupportFragmentManager(), "edit username dialog");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if chosen a image, update the view with new image
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            uploadAvatar(uri);
        } else{
            Toast.makeText(getBaseContext(), "There was an error when fetching image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void sendUsername(String username) {
        updateUsersUsername(username);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navBar.setSelectedItemId(R.id.action_profile);

    }
}
