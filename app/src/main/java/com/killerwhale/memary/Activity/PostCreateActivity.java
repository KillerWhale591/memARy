package com.killerwhale.memary.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.DataModel.Post;
import com.killerwhale.memary.R;

import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Activity for editing and posting new post
 * @author Zeyu Fu
 */
public class PostCreateActivity extends AppCompatActivity {

    private static final String TAG = "NewPostTest";
    private static final float ALPHA_HALF = 0.5f;
    private static final float ALPHA_ORIGINAL = 1f;
    private static final long INTERVAL_LOC_REQUEST = 5000;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 202;
    private static final int PERMISSION_ALL = 1001;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    // Firebase plug-ins
    private FirebaseFirestore db;
    private StorageReference mImagesRef;

    // Location
    private FusedLocationProviderClient FLPC;
    private LocationRequest locationRequest;

    // UI widgets
    private Button btnCancel;
    private Button btnSubmit;
    private ImageButton btnAddImg;
    private SimpleDraweeView imgAttach;
    private ImageButton btnRemove;
    private EditText edtContent;

    // Post variables
    private Uri localUri;
    private String remoteUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);

        // Database init.
        db = FirebaseFirestore.getInstance();
        mImagesRef = FirebaseStorage.getInstance().getReference().child("images");

        // Location
        FLPC = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL_LOC_REQUEST);

        // UI init.
        btnCancel = findViewById(R.id.btnCancel);
        btnAddImg = findViewById(R.id.btnAddImg);
        btnSubmit = findViewById(R.id.btnSubmit);
        imgAttach = findViewById(R.id.imgAttach);
        btnRemove = findViewById(R.id.btnRemove);
        edtContent = findViewById(R.id.edtContent);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAlpha(ALPHA_HALF);
                finish();
            }
        });

        btnAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(getBaseContext(), PERMISSIONS)) {
                    // Permission not granted
                    ActivityCompat.requestPermissions(PostCreateActivity.this,
                            PERMISSIONS,
                            PERMISSION_ALL);
                } else {
                    // Permission has already been granted
                    takePhoto();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEditingEnabled(false);
                if (imgAttach.getVisibility() == View.VISIBLE) {
                    if (localUri != null) {
                        uploadImageAndPost(localUri);
                    }
                } else {
                    submitPost();
                }
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAddingImageEnabled(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showKeyboard();
    }

    @Override
    protected void onPause() {
        hideKeyboard();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                setAddingImageEnabled(false);
                if (data != null) {
                    localUri = data.getData();
                }
                imgAttach.setImageURI(localUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ALL) {
            if (hasGrantedAll(grantResults)) {
                takePhoto();
            }
        }
    }

    /**
     * Helper function for checking permissions
     * @param context activity context
     * @param permissions required permissions
     * @return true if all permissions granted
     */
    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Helper function for checking all permission results
     * @param grantResults all results
     * @return true if all results is granted
     */
    public static boolean hasGrantedAll(int[] grantResults) {
        if (grantResults.length > 0) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Show system keyboard when activity started
     */
    private void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * Hide system keyboard when activity stopped
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /**
     * Start a camera session to take photo
     */
    private void takePhoto() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, REQUEST_CODE_IMAGE_CAPTURE);
    }

    /**
     * Set image related UI when state changed
     * @param enabled true if a user can adding new image
     */
    private void setAddingImageEnabled(boolean enabled) {
        if (enabled) {
            imgAttach.setImageURI((Uri) null);
            imgAttach.setVisibility(View.INVISIBLE);
            btnRemove.setVisibility(View.INVISIBLE);
            btnAddImg.setAlpha(ALPHA_ORIGINAL);
        } else {
            imgAttach.setVisibility(View.VISIBLE);
            btnRemove.setVisibility(View.VISIBLE);
            btnAddImg.setAlpha(ALPHA_HALF);
        }
        btnAddImg.setEnabled(enabled);
    }

    /**
     * Set editing related UI when state changed
     * @param enabled true if a user is not posting
     */
    private void setEditingEnabled(boolean enabled) {
        edtContent.setEnabled(enabled);
        btnSubmit.setEnabled(enabled);
        if (enabled) {
            btnSubmit.setAlpha(ALPHA_ORIGINAL);
        } else {
            btnSubmit.setAlpha(ALPHA_HALF);
        }
    }

    /**
     * Write a new post into database
     */
    private void submitPost() {
        // Get post location
        FLPC.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        }, null);
        FLPC.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String text = edtContent.getText().toString();
                    int type = remoteUrl.isEmpty() ? Post.TYPE_TEXT : Post.TYPE_IMAGE;
                    GeoPoint geo = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Timestamp time = new Timestamp(Calendar.getInstance().getTime());
                    // Create a new post
                    Post newPost = new Post(type, text, remoteUrl, geo, time);
                    Map<String, Object> post = newPost.getHashMap();
                    if (db != null) {
                        db.collection("posts")
                                .add(post)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(PostCreateActivity.this, "Successfully posted", Toast.LENGTH_SHORT).show();
                                        Log.i(TAG, documentReference.getId());
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);
                                    }
                                });
                    }
                }
            }
        });
    }

    /**
     * Upload image to FireBase storage and get url
     * @param uri image file uri
     */
    private void uploadImageAndPost(Uri uri) {

        final StorageReference postImgRef = mImagesRef.child(UUID.randomUUID() + ".jpg");
        UploadTask uploadTask = postImgRef.putFile(uri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return postImgRef.getDownloadUrl();
            }
        });
        urlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        Log.i(TAG, downloadUri.toString());
                        remoteUrl = downloadUri.toString();
                        submitPost();
                    }
                } else {
                    // Handle failures
                    Log.e(TAG, "Upload failed.");
                }
            }
        });
    }
}
