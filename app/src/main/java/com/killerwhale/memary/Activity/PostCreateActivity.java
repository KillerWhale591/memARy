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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.DataModel.Post;
import com.killerwhale.memary.DataModel.User;
import com.killerwhale.memary.Presenter.LocationListAdapter;
import com.killerwhale.memary.R;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Activity for editing and posting new post
 * @author Zeyu Fu
 */
public class PostCreateActivity extends AppCompatActivity {

    private static final String TAG = "NewPostTest";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final float ALPHA_HALF = 0.5f;
    private static final float ALPHA_ORIGINAL = 1f;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 202;
    private static final int PERMISSION_ALL = 1001;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };
    private static final int ACTION_SEARCH_NEARBY = 1995;
    private double[] latlng = {};

    // Firebase plug-ins
    private FirebaseFirestore db;
    private StorageReference mImagesRef;
    private CollectionReference mPostRef;
    private GeoFirestore geoFirestore;
    private String mUid = "";

    // Location
    private Location mLocation;
    private Button btnSearch;

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
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        mImagesRef = FirebaseStorage.getInstance().getReference().child("images");
        mPostRef = db.collection("posts");
        geoFirestore = new GeoFirestore(mPostRef);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUid = user.getUid();
            if (!mUid.isEmpty()) {
                DocumentReference userRef = db.collection("users").document(mUid);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc != null) {
                                String avatar = (String) doc.get(User.FIELD_AVATAR);
                                if (avatar != null) {
                                    SimpleDraweeView icAvatar = findViewById(R.id.icAvatar);
                                    icAvatar.setImageURI(Uri.parse(avatar));
                                }
                            }
                        }
                    }
                });
            }

        }

        // Location
        Intent i = getIntent();
        mLocation = new Location("");
        mLocation.setLatitude(i.getDoubleExtra(KEY_LATITUDE, 0));
        mLocation.setLongitude(i.getDoubleExtra(KEY_LONGITUDE, 0));

        // UI init.
        btnCancel = findViewById(R.id.btnCancel);
        btnAddImg = findViewById(R.id.btnAddImg);
        btnSubmit = findViewById(R.id.btnSubmit);
        imgAttach = findViewById(R.id.imgAttach);
        btnRemove = findViewById(R.id.btnRemove);
        edtContent = findViewById(R.id.edtContent);
        btnSearch = findViewById(R.id.btnSearch);

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
                if (latlng == null) {
                    if (imgAttach.getVisibility() == View.VISIBLE) {
                        if (localUri != null) {
                            uploadImageAndPost(localUri);
                        }
                    } else {
                        if (mLocation != null) {
                            submitPost(mLocation);
                        }
                    }
                }
                else {
                    if (imgAttach.getVisibility() == View.VISIBLE) {
                        if (localUri != null) {
                            uploadImageAndPostAndLocation(localUri, latlng);
                        }
                    } else {
                        if (mLocation != null) {
                            submitPostAndLocation(mLocation, latlng);
                        }
                    }
                }

            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAddingImageEnabled(true);
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostCreateActivity.this, SearchNearbyActivity.class);
                startActivityForResult(intent, ACTION_SEARCH_NEARBY);
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
        else if(requestCode == ACTION_SEARCH_NEARBY){
            if(resultCode == RESULT_OK && data!= null){
                String returnaddress = data.getStringExtra("address");
                latlng = data.getDoubleArrayExtra("latlng");
                btnSearch.setText(returnaddress);
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
                        if (mLocation != null) {
                            submitPost(mLocation);
                        }
                    }
                } else {
                    // Handle failures
                    Log.e(TAG, "Upload failed.");
                }
            }
        });
    }

    /**
     * Submit the post with all the data fields
     * @param location current location
     */
    private void submitPost(Location location) {
        String text = edtContent.getText().toString();
        int type = remoteUrl.isEmpty() ? Post.TYPE_TEXT : Post.TYPE_IMAGE;
        final GeoPoint geo = new GeoPoint(location.getLatitude(), location.getLongitude());
        Timestamp time = new Timestamp(Calendar.getInstance().getTime());
        // Create a new post
        Post newPost = new Post(mUid, type, text, remoteUrl, geo, time);
        Map<String, Object> post = newPost.getHashMap();
        if (db != null) {
            mPostRef.add(post)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(final DocumentReference documentReference) {
                            Toast.makeText(PostCreateActivity.this, "Successfully posted", Toast.LENGTH_SHORT).show();
                            // Log.i(TAG, documentReference.getId());
                            geoFirestore.setLocation(documentReference.getId(), geo);
                            if (!mUid.isEmpty()) {
                                final DocumentReference userRef = db.collection("users").document(mUid);
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            Map<String, Object> data = doc.getData();
                                            if (data != null) {
                                                Object userPosts = data.get(User.FIELD_POSTS);
                                                if (userPosts != null) {
                                                    ((ArrayList<String>) userPosts).add(documentReference.getId());
                                                    userRef.set(data);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
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

    /**
     * Submit the post with all the data fields
     * @param location current location
     */
    private void submitPostAndLocation(Location location, double[] latlng) {
        String text = edtContent.getText().toString();
        int type = remoteUrl.isEmpty() ? Post.TYPE_TEXT : Post.TYPE_IMAGE;
        final GeoPoint geo = new GeoPoint(location.getLatitude(), location.getLongitude());
        Timestamp time = new Timestamp(Calendar.getInstance().getTime());
        // Create a new post
        Post newPost = new Post(mUid, type, text, remoteUrl, geo, time);
        Map<String, Object> post = newPost.getHashMap();
        if (db != null) {
            mPostRef.add(post)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(final DocumentReference documentReference) {
                            Toast.makeText(PostCreateActivity.this, "Successfully posted", Toast.LENGTH_SHORT).show();
                            // Log.i(TAG, documentReference.getId());
                            geoFirestore.setLocation(documentReference.getId(), geo);
                            if (!mUid.isEmpty()) {
                                final DocumentReference userRef = db.collection("users").document(mUid);
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            Map<String, Object> data = doc.getData();
                                            if (data != null) {
                                                Object userPosts = data.get(User.FIELD_POSTS);
                                                if (userPosts != null) {
                                                    ((ArrayList<String>) userPosts).add(documentReference.getId());
                                                    userRef.set(data);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
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
