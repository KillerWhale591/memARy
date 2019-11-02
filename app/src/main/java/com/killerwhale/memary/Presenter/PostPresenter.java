package com.killerwhale.memary.Presenter;

import com.killerwhale.memary.DataModel.Post;

import java.util.ArrayList;

public class PostPresenter {

    private ArrayList<Post> mPosts = new ArrayList<>();
    private long[] ids = new long[] {
            100000,
            100001,
            100002,
            100003,
            100004,
            100005,
            100006
    };
    private int[] types = new int[] {
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
            Post.TYPE_IMAGE,
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
            Post.TYPE_TEXT,
            Post.TYPE_IMAGE,
    };
    private String[] texts = new String[] {
            "Good day",
            "Good day Good day Good day",
            "Good day Good day",
            "Good day Good day Good day Good day Good day Good day",
            "Good day",
            "Good day Good day",
            "Good day Good day Good day"
    };
    private String[] urls = new String[] {
            "",
            "",
            "https://preview.redd.it/k5y6a0x1gzs21.png?width=960&crop=smart&auto=webp&s=a7fd8ec061f386282bbcf380b519a01e1bb2ad43",
            "",
            "",
            "",
            "https://i.ytimg.com/vi/tYBk4kLHPkk/maxresdefault.jpg"
    };
    private long[] locations = new long[] {
            1000000,
            1000001,
            1000002,
            1000003,
            1000004,
            1000005,
            1000006
    };

    public void init() {
        for (int i = 0; i < ids.length; i++) {
            mPosts.add(new Post(ids[i], types[i], texts[i], urls[i], locations[i]));
        }
    }

    public ArrayList<Post> getPosts() {
        return mPosts;
    }
}
