package com.killerwhale.memary.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private SimpleDraweeView icUserInfoAvatar;
    private ImageButton btnCamera;
    private EditText edtUsername;
    private Button btnSubmit;
    private ProgressBar progressUserInfo;
    private FirebaseAuth mAuth;
    private Uri uri = null;
    private StorageReference storageRef;
//    private String remoteUrl = "";

    private static final int PICK_FROM_GALLERY = 9999;
    private static final String TAG = "REDSOX";
    private String Uid;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getBaseContext(), SignInActivity.class));
        } else {
            Uid = currentUser.getUid();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("avatars");

        icUserInfoAvatar = (SimpleDraweeView) findViewById(R.id.icUserInfoAvatar);
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        progressUserInfo = (ProgressBar) findViewById(R.id.progressUserInfo);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UserInfoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString();
                if(TextUtils.isEmpty(username)) {
                    Toast.makeText(getApplicationContext(), "Enter username!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(uri == null){
                    Toast.makeText(getApplicationContext(), "Choose a avatar!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressUserInfo.setVisibility(View.VISIBLE);
                uploadAvatar(username);
                Toast.makeText(getBaseContext(), "uploaded", Toast.LENGTH_SHORT).show();
                progressUserInfo.setVisibility(View.INVISIBLE);
                startActivity(new Intent(getBaseContext(), MainActivity.class));
            }
        });
    }

    private void uploadAvatar(final String username){
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
                        Log.i(TAG, downloadUri.toString());
//                        remoteUrl = downloadUri.toString();
                        createUserDocument(username, downloadUri.toString());
                    }
                } else {
                    // Handle failures
                    Log.e(TAG, "Upload failed.");
                }
            }
        });
    }

    private void createUserDocument(String username, String remoteUrl){
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("avatar", remoteUrl);
        user.put("posts", new ArrayList<DocumentReference>());
        if(db != null) {
            db.collection("users").document(Uid)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            icUserInfoAvatar.setImageURI(uri);
        } else{
            Toast.makeText(getBaseContext(), "There was an error when fetching image", Toast.LENGTH_LONG).show();
        }
    }
}
