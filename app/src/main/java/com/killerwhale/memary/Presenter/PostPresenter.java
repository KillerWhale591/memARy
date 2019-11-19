package com.killerwhale.memary.Presenter;

import android.location.Location;
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
    private ArrayList<Query> geoQueries;
    private Query nextTimeQuery;
    private Query allGeoQuery;
    private Query nextGeoQuery;
    private int mMode;
    private double mRadius;
    private Location mLocation;

    /**
     * Constructor, presenter for showing posts order by recent time
     * @param db database ref
     */
    public PostPresenter(FirebaseFirestore db) {
        this.nextTimeQuery = null;
        this.mPostRef = db.collection("posts");
        this.mMode = MODE_RECENT;
    }

    /**
     * Constructor, presenter for showing nearby posts filter by distance
     * @param db database ref
     * @param radius searching radius
     */
    public PostPresenter(FirebaseFirestore db, Location location, double radius) {
        this.nextGeoQuery = null;
        this.mLocation = location;
        this.mPostRef = db.collection("posts");
        this.geoFirestore = new GeoFirestore(mPostRef);
        this.mRadius = radius;
        this.mMode = MODE_NEARBY;
    }

    /**
     * Initialization. Set present mode
     * @param adapter adapter to present
     * @param refresh true if it is an init. after refresh operation
     */
    public void init(final PostFeedAdapter adapter, final boolean refresh, int mode) {
        mPosts.clear();
        nextTimeQuery = null;
        if (mode == MODE_RECENT) {
            queryByTime(adapter, refresh);
        } else if (mode == MODE_NEARBY) {
            queryByDistance(adapter, refresh, mRadius);
        }
    }

    /**
     * Get all the posts
     * @return post list
     */
    public ArrayList<Post> getPosts() {
        return mPosts;
    }

    /**
     * Load 10 more post when scroll to the bottom
     * @param adapter adapter
     */
    public void load10Posts(final PostFeedAdapter adapter) {
        if (mMode == MODE_RECENT) {
            loadMoreByTime(adapter);
        } else if (mMode == MODE_NEARBY) {
            loadMoreByDistance(adapter);
        }
    }

    /**
     * Get documents from database, query by recent time
     * @param adapter adapter
     * @param refresh is refresh?
     */
    private void queryByTime(final PostFeedAdapter adapter, final boolean refresh) {
        Query timeQuery = mPostRef.orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING).limit(LIMIT_POST);
        timeQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot document : documents) {
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
                        DocumentSnapshot lastVisible = documents.get(queryDocumentSnapshots.size() - 1);
                        nextTimeQuery = mPostRef
                                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(LIMIT_POST);
                    }
                });
    }

    /**
     * Get documents from database, query by nearby radius area
     * @param adapter adapter
     * @param refresh is refresh?
     * @param radius nearby searching radius
     */
    private void queryByDistance(final PostFeedAdapter adapter, final boolean refresh, double radius) {
        GeoQuery geoQuery = geoFirestore.queryAtLocation(
                new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude()), radius);
        geoQueries = geoQuery.getQueries();
        for (final Query query : geoQueries) {
            if (query != null) {
                query.limit(LIMIT_POST).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                                if (documents.size() > 0) {
                                    for (DocumentSnapshot document : documents) {
                                        if (document != null) {
                                            Log.i(TAG, document.getId());
                                            mPosts.add(new Post(document.getData()));
                                        }
                                    }
                                    if (refresh) {
                                        adapter.updateAndStopRefresh();
                                    } else {
                                        adapter.updateView();
                                    }
                                    allGeoQuery = query;
                                    DocumentSnapshot last = documents
                                            .get(queryDocumentSnapshots.size() - 1);
                                    nextGeoQuery = allGeoQuery.startAfter(last).limit(LIMIT_POST);
                                }
                            }
                        });
            }
        }
    }

    private void loadMoreByTime(final PostFeedAdapter adapter) {
        if (nextTimeQuery != null) {
            nextTimeQuery.get()
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
                                nextTimeQuery = mPostRef
                                        .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                                        .startAfter(lastVisible)
                                        .limit(LIMIT_POST);
                            }
                        }
                    });
        }
    }

    private void loadMoreByDistance(final PostFeedAdapter adapter) {
        if (nextGeoQuery != null) {
            nextGeoQuery.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            Log.i(TAG, "size: "+documents.size());
                            if (documents.size() > 0) {
                                for (DocumentSnapshot document : documents) {
                                    Log.i(TAG, document.getId());
                                    mPosts.add(new Post(document.getData()));
                                }
                                adapter.updateView();
                                DocumentSnapshot last = documents.get(queryDocumentSnapshots.size() - 1);
                                nextGeoQuery = allGeoQuery.startAfter(last).limit(LIMIT_POST);
                            }
                        }
                    });
        }
    }
}
