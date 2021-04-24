package com.example.socialize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendsLVAdapter extends RecyclerView.Adapter<FriendsLVHolder> {

    private Context mContext;
    ArrayList<String> names;
    ArrayList<String> keys;
    boolean fromSearch;

    public FriendsLVAdapter(Context mContext, ArrayList<String> names, ArrayList<String> keys, boolean fromSearch) {
        this.mContext = mContext;
        this.names = names;
        this.keys = keys;
        this.fromSearch = fromSearch;
    }

    @NonNull
    @Override
    public FriendsLVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friends_list_item_layout, parent, false);
        FriendsLVHolder friendsLVHolder = new FriendsLVHolder(view);
        return friendsLVHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsLVHolder holder, int position) {

//        holder.dp.setImageResource(R.drawable.fb);
        holder.name.setText(names.get(position));
        holder.setKey(keys.get(position), fromSearch);
    }

    @Override
    public int getItemCount() {
        return names.size();
    }


}
