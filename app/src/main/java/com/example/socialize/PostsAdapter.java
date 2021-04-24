package com.example.socialize;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostsAdapter extends RecyclerView.Adapter<PostHolder> {

    private Context context;
    ArrayList<Posts> posts;
    ArrayList<String> keys;

    public PostsAdapter(Context context, ArrayList<Posts> posts, ArrayList<String> keys) {
        this.context = context;
        this.posts = posts;
        this.keys = keys;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder postHolder, int position) {
        postHolder.setKey(keys.get(position), posts.get(position).getAuthorUID());
        postHolder.postedByNameTV.setText(posts.get(position).getPostBy());
        postHolder.postContentTV.setText(posts.get(position).getPostText());
        postHolder.likes.setText(String.valueOf(posts.get(position).getLikes()));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
