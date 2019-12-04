package com.killerwhale.memary.Presenter;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.killerwhale.memary.DataModel.Post;
import com.killerwhale.memary.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Adapter for the post list (RecyclerView)
 * @author Zeyu Fu
 */
public class PostFeedAdapter extends RecyclerView.Adapter<PostFeedAdapter.PostViewHolder> {

    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_AVATAR = "avatar";

    private Context context;
    private OnRefreshCompleteListener refreshCompleteListener;
    private LinearLayoutManager llm;
    private RecyclerView recyclerView;
    private ArrayList<Post> posts;
    private PostPresenter presenter;
    private Location mLocation;
    private FirebaseFirestore mDatabase;
    private int mMode;

    public PostFeedAdapter(Context aContext, FirebaseFirestore db, RecyclerView rcView, OnRefreshCompleteListener listener) {
        this.context = aContext;
        this.mDatabase = db;
        this.refreshCompleteListener = listener;
        this.llm = (LinearLayoutManager) rcView.getLayoutManager();
        this.recyclerView = rcView;
        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(llm != null && llm.findLastCompletelyVisibleItemPosition() == getItemCount() -1) {
                    //bottom of list!
                    loadMoreData();
                }
            }
        });
        this.posts = new ArrayList<>();
    }

    /**
     * Initialization. Set current location, initialize data presenter
     * @param location current location
     */
    public void init(Location location, int mode) {
        this.mLocation = location;
        this.mMode = mode;
        if (mode == PostPresenter.MODE_RECENT) {
            this.presenter = new PostPresenter(mDatabase);
        } else if (mode == PostPresenter.MODE_NEARBY) {
            this.presenter = new PostPresenter(mDatabase, location, 1);
        }
        this.presenter.init(this, false, mode);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_post_item, parent, false);
        if (type == Post.TYPE_TEXT) {
            // For pure text post, hide image view holder
            ViewGroup.LayoutParams params = view.findViewById(R.id.imgPost).getLayoutParams();
            params.height = 0;
            view.findViewById(R.id.imgPost).setLayoutParams(params);
        }
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int position) {
        // Set text
        String text = posts.get(position).getPostText();
        if (text != null && !text.isEmpty()) {
            postViewHolder.txtPost.setText(posts.get(position).getPostText());
        } else {
            // Hide text box if text is empty
            ViewGroup.LayoutParams params = postViewHolder.txtPost.getLayoutParams();
            params.height = 0;
            postViewHolder.txtPost.setLayoutParams(params);
        }
        // Set image
        String imgUrl = posts.get(position).getImageUrl();
        if (posts.get(position).getType() == Post.TYPE_IMAGE) {
            postViewHolder.imgPost.setImageURI(Uri.parse(imgUrl));
        }
        // Set time
        String time = posts.get(position).getTimeFromNow(Calendar.getInstance().getTime());
        postViewHolder.txtTime.setText(time);
        // Set distance
        String distance = posts.get(position).getDistance(mLocation);
        postViewHolder.txtDistance.setText(distance);
        // Set user info
        String username = posts.get(position).getUsername();
        String avatar = posts.get(position).getAvatar();
        postViewHolder.txtUsername.setText(username);
        if (avatar != null) {
            postViewHolder.imgAvatar.setImageURI(Uri.parse(avatar));
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return posts.get(position).getImageUrl().isEmpty() ? Post.TYPE_TEXT : Post.TYPE_IMAGE;
    }

    /**
     * Scroll to the bottom and load 10 more items
     */
    private void loadMoreData() {
        presenter.load10Posts(this);
    }

    /**
     * Refresh all rows
     */
    public void refreshData() {
        posts.clear();
        presenter.init(this, true, mMode);
    }

    public void updateView() {
        posts = presenter.getPosts();
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void updateAndStopRefresh() {
        posts = presenter.getPosts();
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                refreshCompleteListener.stopRefresh();
            }
        });
    }

    /**
     * Customized ViewHolder for PostList rows
     */
    static class PostViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView imgAvatar;
        SimpleDraweeView imgPost;
        TextView txtUsername;
        TextView txtPost;
        TextView txtTime;
        TextView txtDistance;

        private PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtPost = itemView.findViewById(R.id.txtPost);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDistance = itemView.findViewById(R.id.txtDistance);
        }
    }
}
