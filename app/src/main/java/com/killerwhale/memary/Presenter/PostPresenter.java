package com.killerwhale.memary.Presenter;

import com.killerwhale.memary.DataModel.Post;

import java.util.ArrayList;

/**
 * Presenter of Posts
 * @author Zeyu Fu
 */
public class PostPresenter {

    private ArrayList<Post> mPosts = new ArrayList<>();
    private long[] ids = new long[] {
            100000,
            100001,
            100002,
            100003,
            100004,
            100005,
            100006,
            100007,
            100008,
            100009
    };
    private int[] types = new int[] {
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
            Post.TYPE_IMAGE,
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
            Post.TYPE_IMAGE,
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
    };
    private String[] texts = new String[] {
            "Good day 0",
            "Good day 1",
            "Meme 2",
            "Good day 3",
            "Good day 4",
            "Good day 5",
            "Meme 6",
            "Good day 7",
            "Good day 8",
            "Good day 9"
    };
    private String[] urls = new String[] {
            "",
            "",
            "https://preview.redd.it/k5y6a0x1gzs21.png?width=960&crop=smart&auto=webp&s=a7fd8ec061f386282bbcf380b519a01e1bb2ad43",
            "",
            "",
            "",
            "https://i.ytimg.com/vi/tYBk4kLHPkk/maxresdefault.jpg",
            "",
            "",
            ""
    };
    private long[] locations = new long[] {
            1000000,
            1000001,
            1000002,
            1000003,
            1000004,
            1000005,
            1000006,
            1000007,
            1000008,
            1000009
    };

    public void init() {
        for (int i = 0; i < ids.length; i++) {
            mPosts.add(new Post(ids[i], types[i], texts[i], urls[i], locations[i]));
        }
    }

    public ArrayList<Post> getPosts() {
        return mPosts;
    }

    public void loadMoreData(ArrayList<Post> posts) {
        for (int i = 0; i < ids.length; i++) {
            posts.add(new Post(ids[i], types[i], texts[i], urls[i], locations[i]));
        }
    }
}
