package com.example.socialize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.socialize.MainActivity.bottomNavigationView;
import static com.example.socialize.MainActivity.user;


public class HomeFragment extends Fragment {

    public static final int NUMBER_OF_MAX_POST_LOAD_FOR_A_FRIEND = 3;
    RecyclerView postsRV;
    ArrayList<String> friendsKey;
    private ArrayList<String> friendUIDs;
    ArrayList<Posts> posts;
    ArrayList<String> keys;
    PostsAdapter adapter;
//    FirebaseRecyclerPagingAdapter adapter;
    Query queryForFriendsList, queryForFriendPosts;
    ConstraintLayout loadingCL;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendsKey = new ArrayList<>();
        friendUIDs = new ArrayList<>();
        posts = new ArrayList<>();
        keys = new ArrayList<>();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postsRV = view.findViewById(R.id.newPostsRV);
        loadingCL = view.findViewById(R.id.loadingCL);
        postsRV.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PostsAdapter(getContext(), posts, keys);
        postsRV.setAdapter(adapter);

        queryForFriendsList = FirebaseDatabase.getInstance().getReference()
                .child(getResources().getString(R.string.tableName))
                .child(user.getUid())
                .child(getResources().getString(R.string.friends));

        queryForFriendsList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {

                    Snackbar snackbar = Snackbar.make(view, "Find more friends for more posts!", BaseTransientBottomBar.LENGTH_LONG);
                    snackbar.setAction("Find", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomNavigationView.setSelectedItemId(R.id.nav_friends);
                        }
                    });
                    snackbar.show();

                    Friends friends = snapshot.getValue(Friends.class);
                    friendUIDs = friends.getFriendIDs();

                    for (String uid : friendUIDs) {
                        queryForFriendPosts = FirebaseDatabase.getInstance().getReference()
                                .child(getResources().getString(R.string.tableName))
                                .child(uid)
                                .child(getResources().getString(R.string.posts)).limitToLast(NUMBER_OF_MAX_POST_LOAD_FOR_A_FRIEND);
                        queryForFriendPosts.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Posts userPost = dataSnapshot.getValue(Posts.class);
                                    posts.add(userPost);
                                    keys.add(dataSnapshot.getKey());
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        loadingCL.setVisibility(View.GONE);
                    }
                }
                else {
                    Snackbar snackbar = Snackbar.make(view, "Add friends to see their posts here!", BaseTransientBottomBar.LENGTH_INDEFINITE);
                    snackbar.setAction("Add Friends", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomNavigationView.setSelectedItemId(R.id.nav_friends);
                        }
                    });
                    snackbar.show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}