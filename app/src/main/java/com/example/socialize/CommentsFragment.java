package com.example.socialize;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.socialize.MainActivity.bottomNavigationView;
import static com.example.socialize.MainActivity.user;

public class CommentsFragment extends Fragment {

    final static String UID = "uid", POSTKEY = "postKey";
    String postKey, authorUID;
    CardView addCommentCV;
    EditText commentET;
    public  static FirebaseRecyclerPagingAdapter adapter;
    Query queryForPostComments;
    //    DatabaseReference commentsReference;
    ArrayList<String> keys;
    ArrayList<Comment> comments;
    RecyclerView commentsRV;
    PagedList.Config config;
    DatabasePagingOptions<Comment> options;
    Bundle bundle;
    Button addCommentButton;
    ConstraintLayout backCL, loadingProgress;
    TextView noCommentFoundTV;

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        comments = new ArrayList<>();
        keys = new ArrayList<>();
        bundle = getArguments();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomNavigationView.setVisibility(View.GONE);

        addCommentButton = view.findViewById(R.id.addCommentButton);
        addCommentCV = view.findViewById(R.id.addCommentCV);
        commentET = view.findViewById(R.id.commentET);
        loadingProgress = view.findViewById(R.id.progressBarCL);
        commentsRV = view.findViewById(R.id.commentsRV);
        backCL = view.findViewById(R.id.backActionCL);
        noCommentFoundTV = view.findViewById(R.id.noCommentFoundTV);

        authorUID = bundle.getString(UID);
        postKey = bundle.getString(POSTKEY);
        Log.i("Post Key in CommentsFragment", postKey);

        addCommentCV.setBackgroundResource(R.drawable.nav_bar_design);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        commentsRV.setLayoutManager(manager);

//        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        queryForPostComments = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.tableName))
                .child(authorUID).child(getResources().getString(R.string.posts)).child(postKey)
                .child(getResources().getString(R.string.comments));

        setAdapter();
        addCommentButton.setOnClickListener(addCommentListener);
        backCL.setOnClickListener(backActionListener);
    }

    public void setAdapter(){

        comments.clear();
        keys.clear();

        queryForPostComments.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Log.i("Comments for this post", "Found");
                    noCommentFoundTV.setVisibility(View.INVISIBLE);

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        keys.add(dataSnapshot.getKey());
                        Comment comment = dataSnapshot.getValue(Comment.class);
                        Log.i("Comment", comment.toString());
                        comments.add(comment);
                    }
                }
                else {
                    Log.i("Comments for this post", "Not found");
                    noCommentFoundTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        config = new PagedList.Config.Builder().setEnablePlaceholders(false)
                .setPrefetchDistance(10).setPageSize(20).build();

        options = new DatabasePagingOptions.Builder<Comment>()
                .setLifecycleOwner(this)
                .setQuery(queryForPostComments, config, Comment.class)
                .build();

        adapter = new FirebaseRecyclerPagingAdapter<Comment, CommentHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull CommentHolder commentHolder, int position, @NonNull Comment model) {
                commentHolder.commentUserName.setText(comments.get(position).getCommentUserName());
                commentHolder.commentContent.setText(comments.get(position).getCommentText());
                commentHolder.likeCount.setText(String.valueOf(comments.get(position).getLikes()));
                commentHolder.setKeyAndCommenterUID(keys.get(position), comments.get(position).getCommenterUID(), authorUID, postKey);
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
            public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment, parent, false);
                return new CommentHolder(view);
            }
        };

        commentsRV.setAdapter(adapter);
        adapter.startListening();
    }

    private View.OnClickListener addCommentListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String commentText = commentET.getText().toString();

            if(commentText.length() == 0)
                Toast.makeText(getContext(), "Can't share an empty comment", Toast.LENGTH_SHORT).show();
            else {
                commentET.setText("");
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentET.getWindowToken(), 0);

                queryForPostComments.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        LikedBy likedBy = new LikedBy();
                        likedBy.addToList(user.getUid());

                        if (snapshot.exists()) {
                            Log.i("Comment list", "Found");
                            DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference()
                                    .child(getResources().getString(R.string.tableName))
                                    .child(authorUID).child(getResources().getString(R.string.posts))
                                    .child(postKey).child(getResources().getString(R.string.comments));
                            Comment comment = new Comment(commentText, user.getDisplayName(), user.getUid(), 1, likedBy);
                            commentsReference.push().setValue(comment);
                        } else {
                            Log.i("Comment list", "Not found");
                            DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference()
                                    .child(getResources().getString(R.string.tableName))
                                    .child(authorUID).child(getResources().getString(R.string.posts))
                                    .child(postKey).child(getResources().getString(R.string.comments)).push();
                            Comment comment = new Comment(commentText, user.getDisplayName(), user.getUid(), 1, likedBy);
                            commentsReference.setValue(comment);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Toast.makeText(view.getContext(), "Comment added to post", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener backActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            getActivity().getSupportFragmentManager().popBackStack();
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        bottomNavigationView.setVisibility(View.VISIBLE);
    }
}
