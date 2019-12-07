package com.killerwhale.memary.ARComponent.Utils;

import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.DataModel.ArDrawing;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * Helper class for AR object storage
 * @author Zeyu Fu
 */
public class StrokeStorageHelper {

    private static final String TAG = "strokestring";
    private CollectionReference arRef;
    private GeoFirestore geoFirestore;
    private Location mLocation;

    /**
     * Constructor
     */
    public StrokeStorageHelper() {
        arRef = FirebaseFirestore.getInstance().collection("ar");
        geoFirestore = new GeoFirestore(arRef);
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    /**
     * Download stroke file from FireBase storage
     */
    public void downloadStrokeFile() {

    }

    /**
     * Upload from local file uri to FireBase storage reference
     * @param uri local file uri
     */
    public void uploadStrokeFile(Uri uri) {
        StorageReference strokeRef = FirebaseStorage.getInstance().getReference().child("strokes");
        final StorageReference mStrokeRef = strokeRef.child(UUID.randomUUID() + ".ser");
        UploadTask uploadTask = mStrokeRef.putFile(uri);
        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return mStrokeRef.getDownloadUrl();
            }
        });
        task.addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        String url = task.getResult().toString();
                        Log.i(TAG, "Url: " + url);
                        Map<String, Object> ar = getArObject(url);
                        arRef.add(ar).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    String id = task.getResult().getId();
                                    if (mLocation != null) {
                                        geoFirestore.setLocation(id, new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude()));
                                    }
                                    Log.i(TAG, "Write Success");
                                } else {
                                    Log.i(TAG, "Write Failed");
                                }
                            }
                        });
                    }
                } else {
                    Log.i(TAG, "Store failed");
                }
            }
        });
    }

    /**
     * Build a ar object and get map class
     * @param url stroke file url
     * @return map
     */
    private Map<String, Object> getArObject(String url) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Timestamp time = new Timestamp(Calendar.getInstance().getTime());
        ArDrawing ar = new ArDrawing(user.getUid(), url, time);
        return ar.getHashMap();
    }

    public void searchNearbyAr() {

    }
}
