package com.killerwhale.memary.Presenter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.killerwhale.memary.DataModel.Post;
import com.killerwhale.memary.R;

import java.util.ArrayList;

/**
 * Adapter for the post list (RecyclerView)
 * @author Zeyu Fu
 */
public class PostFeedAdapter extends RecyclerView.Adapter<PostFeedAdapter.PostViewHolder> {

    private static final int VIEW_TYPE_PURE_TEXT = 0;
    private static final int VIEW_TYPE_IMAGE = 1;

    private Context context;
    private LinearLayoutManager llm;
    private RecyclerView recyclerView;
    ArrayList<Post> posts;
    PostPresenter presenter;

    public PostFeedAdapter(Context aContext, RecyclerView rcView) {
        this.context = aContext;
        llm = (LinearLayoutManager) rcView.getLayoutManager();
        recyclerView = rcView;
        presenter = new PostPresenter();
        presenter.init();
        posts = presenter.getPosts();
        rcView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(llm != null && llm.findLastCompletelyVisibleItemPosition() == getItemCount() -1) {
                    //bottom of list!
                    loadMoreData();
                }
            }
        });
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_post_item, parent, false);
        if (type == VIEW_TYPE_PURE_TEXT) {
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
        if (imgUrl != null && !imgUrl.isEmpty()) {
            postViewHolder.imgPost.setImageURI(Uri.parse(imgUrl));
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void loadMoreData() {
        final int prev = getItemCount();
        presenter.loadMoreData(posts);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                notifyItemRangeInserted(prev, 10);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return posts.get(position).getImageUrl().isEmpty() ? VIEW_TYPE_PURE_TEXT : VIEW_TYPE_IMAGE;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView imgAvatar;
        public SimpleDraweeView imgPost;
        public TextView txtUsername;
        public TextView txtPost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtPost = itemView.findViewById(R.id.txtPost);
        }
    }
}
