package com.killerwhale.memary.Presenter;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.killerwhale.memary.DataModel.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter of Posts
 * @author Zeyu Fu
 */
public class PostPresenter {

    private static final String TAG = "GetPostTest";
    private static final int LIMIT_POST = 10;

    private ArrayList<Post> mPosts = new ArrayList<>();
    private FirebaseFirestore mDatabase;
    private Query next;

    public PostPresenter(FirebaseFirestore db) {
        this.mDatabase = db;
    }

    public void init(final PostFeedAdapter adapter, final boolean refresh) {
        mPosts.clear();
        CollectionReference postRef = mDatabase.collection("posts");
        Query first = postRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(LIMIT_POST);
        first.get()
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
                                .get(queryDocumentSnapshots.size() -1);
                        next = mDatabase.collection("posts")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(LIMIT_POST);
                    }
                });
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
                                next = mDatabase.collection("posts")
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .startAfter(lastVisible)
                                        .limit(LIMIT_POST);
                            }
                        }
                    });
        }
    }
}
