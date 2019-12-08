package com.killerwhale.memary.Activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.killerwhale.memary.DataModel.Post;
import com.killerwhale.memary.Presenter.MyPostsAdapter;
import com.killerwhale.memary.Presenter.PostFeedAdapter;
import com.killerwhale.memary.R;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String Uid;
    private RecyclerView myPosts;
    RecyclerView.LayoutManager rvManager;
    private MyPostsAdapter myPostsAdapter;
    private static final String TAG = "BEARS";

    private ArrayList<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myPosts = findViewById(R.id.myPosts);

        posts = new ArrayList<Post>();

    }

    @Override
    protected void onStart() {
        super.onStart();

        // if signed in, get Firebase Auth Uid, else do something
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
//            startActivity(new Intent(getBaseContext(), SignInActivity.class));
        } else {
            Uid = currentUser.getUid();
        }
        rvManager = new LinearLayoutManager(this);
        myPosts.setLayoutManager(rvManager);
        myPosts.addItemDecoration(new DividerItemDecoration(myPosts.getContext(), DividerItemDecoration.VERTICAL));

        db.collection("posts")
                .whereEqualTo("uid", Uid).orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        if (documents.size() > 0) {
                            for (DocumentSnapshot document : documents) {
                                Log.i(TAG, document.getId());
                                Post tempPost = new Post(document.getData());
                                tempPost.setPostId(document.getId());
                                posts.add(tempPost);
                            }
                            System.out.println(posts.size());
                            myPostsAdapter = new MyPostsAdapter(posts, db);
                            myPosts.setAdapter(myPostsAdapter);
                        }
                    }
                });
    }
}
