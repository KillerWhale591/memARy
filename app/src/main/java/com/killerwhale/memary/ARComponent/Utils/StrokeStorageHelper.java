package com.killerwhale.memary.ARComponent.Utils;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.ARComponent.Listener.OnArDownloadedListener;
import com.killerwhale.memary.ARComponent.Listener.OnStrokeUrlCompleteListener;
import com.killerwhale.memary.ARComponent.Model.Stroke;
import com.killerwhale.memary.DataModel.ArDrawing;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
    private FirebaseStorage mStorage;
    private GeoFirestore geoFirestore;
    private Location mLocation;
    private List<List<Stroke>> allARs;
    private ArrayList<String> strokeUrls;
    private OnStrokeUrlCompleteListener onStrokeUrlCompleteListener;
    private OnArDownloadedListener onArDownloadedListener;

    /**
     * Constructor
     */
    public StrokeStorageHelper(Context aContext) {
        arRef = FirebaseFirestore.getInstance().collection("ar");
        mStorage = FirebaseStorage.getInstance();
        geoFirestore = new GeoFirestore(arRef);
        allARs = new ArrayList<>();
        strokeUrls = new ArrayList<>();
        onStrokeUrlCompleteListener = (OnStrokeUrlCompleteListener) aContext;
        onArDownloadedListener = (OnArDownloadedListener) aContext;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    /**
     * Download stroke file from FireBase storage
     */
    public void downloadStrokeFiles() {
        allARs.clear();
        if (!strokeUrls.isEmpty()) {
            final int[] downloaded = {0};
            for (String url : strokeUrls) {
                Log.i(TAG, url);
                StorageReference mStrokeRef = mStorage.getReferenceFromUrl(url);
                mStrokeRef.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                        try {
                            InputStream inputStream = taskSnapshot.getStream();
                            ObjectInputStream in = new ObjectInputStream(inputStream);
                            List<Stroke> oneAr = (List<Stroke>) in.readObject();
                            allARs.add(oneAr);
                            downloaded[0] += 1;
                            // If all download tasks are finished
                            if (downloaded[0] == strokeUrls.size()) {
                                onArDownloadedListener.setStrokeList(allARs);
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
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

    /**
     * Search nearby ar objects and write downloadable url to list
     * @param location current user location
     * @param radius search radius
     */
    public void searchNearbyAr(Location location, double radius, long limit) {
        GeoPoint currentGeo = new GeoPoint(location.getLatitude(), location.getLongitude());
        GeoQuery geoQuery = geoFirestore.queryAtLocation(currentGeo, radius);
        ArrayList<Query> arQueries = geoQuery.getQueries();
        for (final Query query : arQueries) {
            if (query != null) {
                query.limit(limit).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        if (documents.size() > 0) {
                            for (DocumentSnapshot document : documents) {
                                String strokeUrl = (String) document.get(ArDrawing.FIELD_STROKE);
                                strokeUrls.add(strokeUrl);
                            }
                            onStrokeUrlCompleteListener.startDownloadStrokes();
                        }
                    }
                });
            }
        }
    }
}
