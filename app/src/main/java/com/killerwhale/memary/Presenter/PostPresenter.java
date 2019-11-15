package com.killerwhale.memary.Presenter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.killerwhale.memary.DataModel.Post;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Presenter of Posts
 * @author Zeyu Fu
 */
public class PostPresenter {

    private static final String TAG = "GetPostTest";
    private static final int LIMIT_POST = 10;

    private ArrayList<Post> mPosts = new ArrayList<>();
    private FirebaseFirestore mDatabase;

    public PostPresenter(FirebaseFirestore db) {
        this.mDatabase = db;
    }

    public void init(final PostFeedAdapter adapter, final boolean refresh) {
        mPosts.clear();
        CollectionReference postRef = mDatabase.collection("posts");
        postRef.limit(LIMIT_POST).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.i(TAG, document.getId());
                                mPosts.add(new Post(document.getData()));
                            }
                        } else {
                            Log.i(TAG, "Error getting documents: ", task.getException());
                        }
                        if (refresh) {
                            adapter.updateAndStopRefresh();
                        } else {
                            adapter.updateView();
                        }
                    }
                });
    }

    public ArrayList<Post> getPosts() {
        return mPosts;
    }

    public void load10Posts(final PostFeedAdapter adapter) {
        CollectionReference postRef = mDatabase.collection("posts");
        postRef.limit(LIMIT_POST).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.i(TAG, document.getId());
                                mPosts.add(new Post(document.getData()));
                            }
                            adapter.updateView();
                        } else {
                            Log.i(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


//    private long[] ids = new long[] {
//            100000,
//            100001,
//            100002,
//            100003,
//            100004,
//            100005,
//            100006,
//            100007,
//            100008,
//            100009
//    };
//    private int[] types = new int[] {
//            Post.TYPE_TEXT,
//            Post.TYPE_TEXT,
//            Post.TYPE_IMAGE,
//            Post.TYPE_TEXT,
//            Post.TYPE_TEXT,
//            Post.TYPE_TEXT,
//            Post.TYPE_IMAGE,
//            Post.TYPE_TEXT,
//            Post.TYPE_TEXT,
//            Post.TYPE_TEXT,
//    };
//    private String[] texts = new String[] {
//            "Good day 0",
//            "Good day 1",
//            "Meme 2",
//            "Good day 3",
//            "Good day 4",
//            "Good day 5",
//            "Meme 6",
//            "Good day 7",
//            "Good day 8",
//            "Good day 9"
//    };
//    private String[] urls = new String[] {
//            "",
//            "",
//            "https://preview.redd.it/k5y6a0x1gzs21.png?width=960&crop=smart&auto=webp&s=a7fd8ec061f386282bbcf380b519a01e1bb2ad43",
//            "",
//            "",
//            "",
//            "https://i.ytimg.com/vi/tYBk4kLHPkk/maxresdefault.jpg",
//            "",
//            "",
//            ""
//    };
//    private long[] locations = new long[] {
//            1000000,
//            1000001,
//            1000002,
//            1000003,
//            1000004,
//            1000005,
//            1000006,
//            1000007,
//            1000008,
//            1000009
//    };
}
