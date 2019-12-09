package com.killerwhale.memary.Presenter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.killerwhale.memary.DataModel.Post;
import com.killerwhale.memary.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.MyPostsViewHolder> {

    private ArrayList<Post> posts;
    private FirebaseFirestore db;
    private static final String TAG = "COLTS";

    public MyPostsAdapter(ArrayList<Post> posts, FirebaseFirestore db){
        this.posts = posts;
        this.db = db;
    }

    @NonNull
    @Override
    public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_my_post_item, parent, false);
        if (type == Post.TYPE_TEXT) {
            // For pure text post, hide image view holder
            ViewGroup.LayoutParams params = view.findViewById(R.id.imgPost).getLayoutParams();
            params.height = 0;
            view.findViewById(R.id.imgPost).setLayoutParams(params);
        }
        return new MyPostsAdapter.MyPostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostsViewHolder viewHolder, final int position) {
        String text = posts.get(position).getPostText();
        if (text != null && !text.isEmpty()) {
            viewHolder.txtPost.setText(posts.get(position).getPostText());
        } else {
            // Hide text box if text is empty
            ViewGroup.LayoutParams params = viewHolder.txtPost.getLayoutParams();
            params.height = 0;
            viewHolder.txtPost.setLayoutParams(params);
        }

        String imgUrl = posts.get(position).getImageUrl();
        if (posts.get(position).getType() == Post.TYPE_IMAGE) {
            viewHolder.imgPost.setImageURI(Uri.parse(imgUrl));
        }

        String time = posts.get(position).getTimeFromNow(Calendar.getInstance().getTime());
        viewHolder.txtTime.setText(time);

        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Post deletePost = posts.get(position);
                posts.remove(position);
                System.out.println(deletePost.getPostId());
                db.collection("posts").document(deletePost.getPostId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });

                db.collection("location")
                        .whereArrayContains("posts", deletePost.getPostId())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                                if (documents.size() > 0) {
                                    for (DocumentSnapshot document : documents) {
                                        ArrayList<String> locationPosts = (ArrayList<String>) document.get("posts");
                                        locationPosts.remove(deletePost.getPostId());
                                        int listSize = locationPosts.size();
                                        if(listSize == 0){
                                            db.collection("location").document(document.getId())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.i(TAG, "Location DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error deleting location", e);
                                                    }
                                                });
                                        } else{
                                            db.collection("location").document(document.getId()).update("numPosts", listSize);
                                            db.collection("location").document(document.getId()).update("posts", locationPosts);
                                            Log.i(TAG, "successfully updated location!");
                                        }
                                    }
                                }
                            }
                        });

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, posts.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return posts.get(position).getImageUrl().isEmpty() ? Post.TYPE_TEXT : Post.TYPE_IMAGE;
    }

    static class MyPostsViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView imgPost;
        TextView txtPost;
        TextView txtTime;
        ImageButton btnDelete;

        private MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtPost = itemView.findViewById(R.id.txtPost);
            txtTime = itemView.findViewById(R.id.txtTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
