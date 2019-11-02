package com.killerwhale.memary.Presenter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.killerwhale.memary.DataModel.Post;
import com.killerwhale.memary.R;

import java.util.ArrayList;

public class PostFeedAdapter extends RecyclerView.Adapter<PostFeedAdapter.PostViewHolder> {

    private Context context;
    ArrayList<Post> posts;
    PostPresenter presenter;

    public PostFeedAdapter(Context aContext) {
        this.context = aContext;
        presenter = new PostPresenter();
        presenter.init();
        posts = presenter.getPosts();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_post_list, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int position) {
        postViewHolder.txtUsername.setText("Username");
        postViewHolder.txtPost.setText(posts.get(position).getPostText());
        String imgUrl = posts.get(position).getImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            postViewHolder.imgPost.setImageURI(Uri.parse(imgUrl));
        } else {
            ViewGroup.LayoutParams params = postViewHolder.imgPost.getLayoutParams();
            params.height = 0;
            postViewHolder.imgPost.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
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
