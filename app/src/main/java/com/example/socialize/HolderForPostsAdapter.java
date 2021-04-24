package com.example.socialize;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HolderForPostsAdapter extends RecyclerView.ViewHolder {

    ImageView postedByUserDP, likeIcon;
    TextView postedByNameTV, likes, postContentTV;

    public HolderForPostsAdapter(@NonNull final View itemView) {
        super(itemView);

        this.postedByUserDP = itemView.findViewById(R.id.postedByUserDP);
        this.likeIcon = itemView.findViewById(R.id.postLikeIcon);
        this.postedByNameTV = itemView.findViewById(R.id.postedByNameTV);
        this.postContentTV = itemView.findViewById(R.id.postContentTV);
        this.likes = itemView.findViewById(R.id.postLikeCount);

        /*databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("posts").child(key);//.child("likedBy");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("Snapshot for likedBVy arraylist", snapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/


        Log.i("Like Icon Tag before clicking", likeIcon.getTag().toString());

        likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Log.i("Like Icon Tag when clicked", likeIcon.getTag().toString());

//                databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("posts").child(key).child("likes");
                int count = Integer.parseInt(likes.getText().toString());
                switch (view.getTag().toString()) {

                    case "like":
                        Toast.makeText(itemView.getContext(), "Liked!", Toast.LENGTH_SHORT).show();
                        Log.i("Type", "like");
                        likeIcon.setImageResource(R.drawable.liked);
                        ++count;
                        Log.i("Count", String.valueOf(count));
                        likeIcon.setTag("liked");
                        likes.setText(String.valueOf(count));
//                        databaseReference.setValue(count);
                        Log.i("Like Icon Tag after click", likeIcon.getTag().toString());
//                            updateLikes("like");
                        break;

                    case "liked":
                        Toast.makeText(itemView.getContext(), "Like removed!", Toast.LENGTH_SHORT).show();
                        Log.i("Type", "remove-like");
                        likeIcon.setImageResource(R.drawable.like);

                        --count;
                        Log.i("Count", String.valueOf(count));
                        likes.setText(String.valueOf(count));
                        likeIcon.setTag("like");
//                        databaseReference.setValue(count);
                        Log.i("Like Icon Tag after click", likeIcon.getTag().toString());
//                        updateLikes("remove-like");
                        break;
                }
            }
        });

    }

}