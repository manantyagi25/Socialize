package com.example.socialize;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static com.example.socialize.MainActivity.user;


public class ViewAccountFragment extends Fragment {

    final static String UID = "UID";
    Bundle bundle;
    String searchedUID, fullName;
    TextView searchedUserName, noPostFoundTV;
    ImageView userDP;

    Button addFriend;
    ArrayList<Posts> posts;
    ArrayList<String> keys;
    RecyclerView viewUserPostsRV;
    ConstraintLayout loadingProgress;
    public  static FirebaseRecyclerPagingAdapter adapter;
    DatabaseReference friendsReference;
    boolean isFriend = false, friendListExist;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_viewaccount, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getArguments();
        posts = new ArrayList<>();
        keys = new ArrayList<>();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchedUserName = view.findViewById(R.id.searchedUserNameTV);
        noPostFoundTV = view.findViewById(R.id.noPostFoundTV);
        userDP = view.findViewById(R.id.searchedUserDP);
        addFriend = view.findViewById(R.id.addFriendButton);
        loadingProgress = view.findViewById(R.id.progressBarCL);
        viewUserPostsRV = view.findViewById(R.id.viewUserPostsRV);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        viewUserPostsRV.setLayoutManager(manager);

        searchedUID = bundle.getString(UID);
        Query queryForUserProfileData = FirebaseDatabase.getInstance().getReference().child("users").child(searchedUID);
        queryForUserProfileData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                fullName = snapshot.child("fullName").getValue().toString();
                searchedUserName.setText(fullName);
                Log.i("Name", fullName);

                File path = new File(getContext().getExternalFilesDir(null).toString(), "/profilePics/");
                File pathToProfilePic = new File(path, searchedUID);
                Log.i("Path", pathToProfilePic.toString());

                if (pathToProfilePic.exists()) {
                    Log.i("File", "exists");
                    Bitmap dp = BitmapFactory.decodeFile(pathToProfilePic.toString());
                    Bitmap resized = Bitmap.createScaledBitmap(dp, (int)(dp.getWidth()*0.5), (int)(dp.getHeight()*0.5), true);
                    userDP.setImageBitmap(resized);
                } else {
                    Log.i("File", "not exists");
                    StorageReference reference = FirebaseStorage.getInstance().getReference();
                    reference.child(searchedUID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(userDP);

                            Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap1, Picasso.LoadedFrom from) {

                                BitmapDrawable drawable = (BitmapDrawable) userDP.getDrawable();
                                Bitmap bitmap = drawable.getBitmap();
                                try {
                                    File file = new File(getContext().getExternalFilesDir(null).toString());
                                    File myDir = new File(file, "profilePics");
                                    if (!myDir.exists())
                                        myDir.mkdirs();

                                    myDir = new File(myDir, searchedUID);
                                    FileOutputStream outputStream = new FileOutputStream(myDir);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {}
                        };

                        Picasso.get().load(uri).into(target);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        friendsReference = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.tableName))
                .child(user.getUid()).child("friends");
        friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Log.i("Friends List for this user", "Exists");
                    Friends friends = snapshot.getValue(Friends.class);
                    isFriend = friends.checkIfUserExistsInFriendsList(searchedUID);
                    if(isFriend) {
                        addFriend.setText(getResources().getString(R.string.alreadyFriends));
                        addFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
                    }
                    else {
                        addFriend.setText(getResources().getString(R.string.addFriend));
                        addFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_addfriend_24, 0, 0, 0);
                    }
                }
                else {
                    Log.i("Friends List for this user", "Does not exist");
                    addFriend.setText(getResources().getString(R.string.addFriend));
                    addFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_addfriend_24, 0, 0, 0);
                }

                addFriend.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(!isFriend) {
                    addFriend.setText(R.string.alreadyFriends);
                    addFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
                    isFriend = true;
                    friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.i("Friends List for this user", "Exists");
                                Friends friends = snapshot.getValue(Friends.class);
                                friends.addFriendToList(searchedUID);
                                friendsReference.setValue(friends);
                            } else {
                                Log.i("Friends List for this user", "Does not exist");
                                Friends friends = new Friends();
                                friends.addFriendToList(searchedUID);
                                friendsReference.setValue(friends);
                            }

                            final DatabaseReference userFriendReference = FirebaseDatabase.getInstance().getReference()
                                    .child(getResources().getString(R.string.tableName))
                                    .child(searchedUID).child("friends");
                            userFriendReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Log.i("Friends List for this user", "Exists");
                                        Friends friends = snapshot.getValue(Friends.class);
                                        friends.addFriendToList(user.getUid());
                                        userFriendReference.setValue(friends);
                                    }
                                    else {
                                        Log.i("Friends List for this user", "Does not exist");
                                        Friends friends = new Friends();
                                        friends.addFriendToList(user.getUid());
                                        userFriendReference.setValue(friends);
                                    }
                                    addFriend.setText(R.string.alreadyFriends);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                else {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Remove Friend?")
                            .setMessage("You can always add them as friend later")
                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Toast.makeText(view.getContext(), searchedUserName.getText().toString() + " removed from your Friends", Toast.LENGTH_SHORT).show();
                                    addFriend.setText(getResources().getString(R.string.addFriend));
                                    addFriend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_addfriend_24, 0, 0, 0);
                                    isFriend = false;

                                    final DatabaseReference userFriendListReference = FirebaseDatabase.getInstance().getReference()
                                            .child(view.getResources().getString(R.string.tableName))
                                            .child(user.getUid())
                                            .child(view.getResources().getString(R.string.friends));
                                    userFriendListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    Log.i("Friends List Snapshot", snapshot.toString());
                                            Friends friends = snapshot.getValue(Friends.class);
                                            friends.removeFriendFromList(searchedUID);
                                            userFriendListReference.setValue(friends);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    final DatabaseReference keyFriendListReference = FirebaseDatabase.getInstance().getReference()
                                            .child(view.getResources().getString(R.string.tableName))
                                            .child(searchedUID)
                                            .child(view.getResources().getString(R.string.friends));
                                    keyFriendListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    Log.i("Friends List Snapshot", snapshot.toString());
                                            Friends friends = snapshot.getValue(Friends.class);
                                            friends.removeFriendFromList(user.getUid());
                                            keyFriendListReference.setValue(friends);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
        });

        Query postsQuery = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.tableName))
                .child(searchedUID).child(getResources().getString(R.string.posts));
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    noPostFoundTV.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Posts userPost = dataSnapshot.getValue(Posts.class);
                        posts.add(userPost);
                        keys.add(dataSnapshot.getKey());
                    }
                }
                else {
                    noPostFoundTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        PagedList.Config config = new PagedList.Config.Builder().setEnablePlaceholders(false).setPrefetchDistance(10).setPageSize(20).build();

        DatabasePagingOptions<Posts> options = new DatabasePagingOptions.Builder<Posts>()
                .setLifecycleOwner(this)
                .setQuery(postsQuery, config, Posts.class)
                .build();

        adapter = new FirebaseRecyclerPagingAdapter<Posts, PostHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostHolder postHolder, int position, @NonNull Posts model) {
                postHolder.postedByUserDP.setImageResource(R.drawable.socialize);
                postHolder.postedByNameTV.setText(posts.get(position).getPostBy());
                postHolder.postContentTV.setText(posts.get(position).getPostText());
                postHolder.likes.setText(String.valueOf(posts.get(position).getLikes()));
                Log.i("Key value for post", keys.get(position));
                postHolder.setKey(keys.get(position), posts.get(position).getAuthorUID());
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

        viewUserPostsRV.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
