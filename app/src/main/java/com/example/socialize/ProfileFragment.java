package com.example.socialize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.socialize.MainActivity.downloadAndSetUserDP;
import static com.example.socialize.MainActivity.preferences;
import static com.example.socialize.MainActivity.user;

public class ProfileFragment extends Fragment {

    ImageView dp;
    TextView userName;
    RecyclerView allUserPostsRV;
    ArrayList<Posts> posts;
    ArrayList<String> keys;
    Query queryForUserPosts;
    ConstraintLayout loadingProgress;
    FloatingActionButton createNewPostButton;
    public  static FirebaseRecyclerPagingAdapter adapter;
    Spinner spinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        posts = new ArrayList<>();
        keys = new ArrayList<>();
        queryForUserPosts = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("posts");
        Log.i("Query", queryForUserPosts.toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dp = view.findViewById(R.id.userDP);
        userName = view.findViewById(R.id.userNameTV);
        allUserPostsRV = view.findViewById(R.id.allUserPostsRV);
        loadingProgress = view.findViewById(R.id.progressBarCL);
        createNewPostButton = view.findViewById(R.id.newPostFAB);
        createNewPostButton.setOnClickListener(newPostButtonListener);
        spinner = view.findViewById(R.id.spinner);

        downloadAndSetUserDP(dp, user.getUid(), getContext());
        userName.setText(preferences.getString("name", ""));

        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(true);
        allUserPostsRV.setLayoutManager(manager);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.dropDownListMenu, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                                                  Log.i("Spinner Item Selected", adapterView.getItemAtPosition(i).toString());
                                                  if(i == 0)
                                                      manager.setReverseLayout(false);
                                                  else
                                                      manager.setReverseLayout(true);
                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> adapterView) {

                                              }
                                          });

                queryForUserPosts.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Posts userPost = dataSnapshot.getValue(Posts.class);
                            posts.add(userPost);
                            keys.add(dataSnapshot.getKey());
                        }
                        Log.i("Key while fetching posts", keys.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        setAdapter();
    }

    public void setAdapter(){

        PagedList.Config config = new PagedList.Config.Builder().setEnablePlaceholders(false).setPrefetchDistance(10).setPageSize(20).build();

        DatabasePagingOptions<Posts> options = new DatabasePagingOptions.Builder<Posts>()
                .setLifecycleOwner(this)
                .setQuery(queryForUserPosts, config, Posts.class)
                .build();

        adapter = new FirebaseRecyclerPagingAdapter<Posts, PostHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostHolder postHolder, int position, @NonNull Posts model) {
                postHolder.setKey(keys.get(position), posts.get(position).getAuthorUID());
                postHolder.postedByNameTV.setText(posts.get(position).getPostBy());
                postHolder.postContentTV.setText(posts.get(position).getPostText());
                postHolder.likes.setText(String.valueOf(posts.get(position).getLikes()));
                Log.i("Key value for post", keys.get(position));
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state){
                    case LOADED:
                        loadingProgress.setVisibility(View.GONE);
                        break;

                    case ERROR:
                        Toast.makeText(getContext(), "Error loading data!", Toast.LENGTH_SHORT).show();
                        loadingProgress.setVisibility(View.GONE);
                        break;
                }
            }

            @NonNull
            @Override
            public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post, parent, false);
                return new PostHolder(view);
            }
        };

        allUserPostsRV.setAdapter(adapter);

    }

    View.OnClickListener newPostButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getContext(), CreateOrEditActivity.class);
            intent.putExtra(CreateOrEditActivity.CONTENT_TYPE, 0);
            startActivity(intent);
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}