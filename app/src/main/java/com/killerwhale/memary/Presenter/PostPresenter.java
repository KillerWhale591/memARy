package com.killerwhale.memary.Presenter;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.killerwhale.memary.DataModel.Post;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter of Posts
 * @author Zeyu Fu
 */
public class PostPresenter {

    public static final int MODE_RECENT = 0;
    public static final int MODE_NEARBY = 1;

    private static final String TAG = "GetPostTest";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final int LIMIT_POST = 10;

    private ArrayList<Post> mPosts = new ArrayList<>();
    private CollectionReference mPostRef;
    private GeoFirestore geoFirestore;
    private Query next;
    private int mMode;
    private double mRadius;

    public PostPresenter(FirebaseFirestore db) {
        this.mPostRef = db.collection("posts");
        this.mMode = MODE_RECENT;
    }

    public PostPresenter(FirebaseFirestore db, double radius) {
        this.mPostRef = db.collection("posts");
        this.geoFirestore = new GeoFirestore(mPostRef);
        this.mRadius = radius;
        this.mMode = MODE_NEARBY;
    }

    public void init(final PostFeedAdapter adapter, final boolean refresh) {
        mPosts.clear();
        next = null;
        if (mMode == MODE_RECENT) {
            queryByTime(adapter, refresh);
        } else if (mMode == MODE_NEARBY) {
            queryByDistance(adapter, refresh);
        }
    }

    public ArrayList<Post> getPosts() {
        return mPosts;
    }

    public void load10Posts(final PostFeedAdapter adapter) {
        if (next != null) {
            next.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            if (documents.size() > 0) {
                                for (DocumentSnapshot document : documents) {
                                    Log.i(TAG, document.getId());
                                    mPosts.add(new Post(document.getData()));
                                }
                                adapter.updateView();
                                // Construct a new query starting at this document,
                                // get the next 10 posts.
                                DocumentSnapshot lastVisible = queryDocumentSnapshots.getDocuments()
                                        .get(queryDocumentSnapshots.size() -1);
                                next = mPostRef
                                        .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                                        .startAfter(lastVisible)
                                        .limit(LIMIT_POST);
                            }
                        }
                    });
        }
    }

    private void queryByTime(final PostFeedAdapter adapter, final boolean refresh) {
        Query timeQuery = mPostRef.orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING).limit(LIMIT_POST);
        timeQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Log.i(TAG, document.getId());
                            mPosts.add(new Post(document.getData()));
                        }
                        if (refresh) {
                            adapter.updateAndStopRefresh();
                        } else {
                            adapter.updateView();
                        }
                        // Construct a new query starting at this document,
                        // get the next 10 posts.
                        DocumentSnapshot lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        next = mPostRef
                                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(LIMIT_POST);
                    }
                });
    }

    private void queryByDistance(final PostFeedAdapter adapter, final boolean refresh) {
        GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(42, -71), mRadius);
        for (Query query : geoQuery.getQueries()) {
            query.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Log.i(TAG, document.getId());
                                mPosts.add(new Post(document.getData()));
                            }
                            if (refresh) {
                                adapter.updateAndStopRefresh();
                            } else {
                                adapter.updateView();
                            }
                        }
                    });
        }
    }
}
