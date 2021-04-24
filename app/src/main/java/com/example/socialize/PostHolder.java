package com.example.socialize;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.socialize.MainActivity.bottomNavigationView;
import static com.example.socialize.MainActivity.user;

public class PostHolder extends RecyclerView.ViewHolder {

    CircleImageView postedByUserDP;
    ImageView likeIcon, optionsMenu;
    TextView postedByNameTV, likes, postContentTV;
    public String key;
    public String authorUID;
    public ConstraintLayout commentsButton;
    private Context context;
    private AlertDialog dialog;

    public PostHolder(@NonNull final View itemView) {
        super(itemView);

        this.postedByUserDP = itemView.findViewById(R.id.postedByUserDP);
        this.likeIcon = itemView.findViewById(R.id.postLikeIcon);
        this.postedByNameTV = itemView.findViewById(R.id.postedByNameTV);
        this.postContentTV = itemView.findViewById(R.id.postContentTV);
        this.likes = itemView.findViewById(R.id.postLikeCount);
        this.commentsButton = itemView.findViewById(R.id.commentTray);
        this.optionsMenu = itemView.findViewById(R.id.optionsMenuIVPost);
        this.context = itemView.getContext();


        likeIcon.setOnClickListener(likeIconListener);
        commentsButton.setOnClickListener(commentButtonListener);
        postedByNameTV.setOnClickListener(viewAccountListener);
        postedByUserDP.setOnClickListener(viewAccountListener);
        optionsMenu.setOnClickListener(menuListener);

    }

    public void setKey(final String key, final String authorUID){
        this.key = key;
        this.authorUID = authorUID;

        downloadAndSetUserDP();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(context.getResources().getString(R.string.tableName))
                .child(authorUID)
                .child(context.getResources().getString(R.string.posts))
                .child(key)
                .child("likedBy");      // Query to retrieve likedBy list from Firebase

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Log.i("Liked By", "Exists");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {     //listener to query
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            LikedBy likedBy = snapshot.getValue(LikedBy.class);                 //getting likedBy arraylist

                            if (likedBy.checkAuthorLike(user.getUid())) {       //checking if author has liked the post
                                likeIcon.setImageResource(R.drawable.liked);    //setting image liked drawable
                            } else {
                                likeIcon.setImageResource(R.drawable.like);    //setting image liked drawable
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else {
                    likeIcon.setImageResource(R.drawable.like);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void downloadAndSetUserDP() {
        File path = new File(context.getExternalFilesDir(null).toString(), "/profilePics/");
        File pathToProfilePic = new File(path, authorUID);
        Log.i("Path", pathToProfilePic.toString());
        if (pathToProfilePic.exists()) {
            Log.i("File", "exists");
            Bitmap dp = BitmapFactory.decodeFile(pathToProfilePic.toString());
            Bitmap resized = Bitmap.createScaledBitmap(dp, (int)(dp.getWidth()*0.1), (int)(dp.getHeight()*0.1), true);
            postedByUserDP.setImageBitmap(resized);
        }
        else {
            Log.i("File", "not exists");
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(key).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //                        profilePic.setImageBitmap(uri.getPath());
                            Picasso.get().load(uri).into(postedByUserDP);

                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap1, Picasso.LoadedFrom from) {

                                    BitmapDrawable drawable = (BitmapDrawable) postedByUserDP.getDrawable();
                                    Bitmap bitmap = drawable.getBitmap();
                                    try {
                                        File file = new File(context.getExternalFilesDir(null).toString());
                                        File myDir = new File(file, "profilePics");
                                        if (!myDir.exists())
                                            myDir.mkdirs();

                                        myDir = new File(myDir, authorUID);
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
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            };

                            Picasso.get().load(uri).into(target);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            postedByUserDP.setImageResource(R.drawable.socialize);
                            Toast.makeText(context, "Error downloading profile pic!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    View.OnClickListener likeIconListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(authorUID)
                    .child("posts").child(key).child("likedBy");      // Query to retrieve likedBy list from Firebase

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Log.i("Liked By", "Exists");
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {     //listener to query
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                LikedBy likedBy = snapshot.getValue(LikedBy.class);                 //getting likedBy arraylist
                                final DatabaseReference likesReference = FirebaseDatabase.getInstance().getReference().child("users").child(authorUID)
                                        .child("posts").child(key).child("likes");   // Reference to likesCount in firebase


                                if (likedBy.checkAuthorLike(user.getUid())) {       //checking if author has liked the post
                                    likedBy.removeFromList(user.getUid());          //removing author like
                                    likeIcon.setImageResource(R.drawable.like);
                                    int likeCount = Integer.parseInt(likes.getText().toString());
                                    likes.setText(String.valueOf(--likeCount));
                                    likesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            long likesCountFromServer = (long) snapshot.getValue();
                                            --likesCountFromServer;
                                            likesReference.setValue(likesCountFromServer);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    likedBy.addToList(user.getUid());               //adding author like
                                    likeIcon.setImageResource(R.drawable.liked);
                                    int likeCount = Integer.parseInt(likes.getText().toString());
                                    likes.setText(String.valueOf(++likeCount));
                                    likesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            long likesCountFromServer = (long) snapshot.getValue();
                                            ++likesCountFromServer;
                                            likesReference.setValue(likesCountFromServer);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }

                                reference.setValue(likedBy);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    else {
                        Log.i("Liked By", "Does not exist");
                        LikedBy likedBy = new LikedBy();
                        likedBy.addToList(user.getUid());
                        likeIcon.setImageResource(R.drawable.liked);
                        int likeCount = Integer.parseInt(likes.getText().toString());
                        likes.setText(String.valueOf(++likeCount));
                        reference.setValue(likedBy);
                        DatabaseReference likesReference = FirebaseDatabase.getInstance().getReference().child("users").child(authorUID)
                                .child("posts").child(key).child("likes");
                        likesReference.setValue(1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };

    View.OnClickListener commentButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CommentsFragment commentsFragment = new CommentsFragment();
            Bundle args = new Bundle();
            args.putString(CommentsFragment.UID, authorUID);
            args.putString(CommentsFragment.POSTKEY, key);
            commentsFragment.setArguments(args);
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, commentsFragment).addToBackStack(null).commit();
        }
    };

    View.OnClickListener viewAccountListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            if (!authorUID.equals(user.getUid())) {
                ViewAccountFragment viewAccountFragment = new ViewAccountFragment();
                Bundle args = new Bundle();
                args.putString(ViewAccountFragment.UID, authorUID);
                viewAccountFragment.setArguments(args);

                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, viewAccountFragment)
                        .addToBackStack(null).commit();
            }
            else {
                bottomNavigationView.setSelectedItemId(R.id.nav_accountData);
            }
        }
    };

    View.OnClickListener menuListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            LayoutInflater inflater = LayoutInflater.from(context);
            final View menu = inflater.inflate(R.layout.owner_menu_layout_post, null, false);
            if(authorUID.equals(user.getUid())) {
                dialog = new AlertDialog.Builder(view.getContext())
                        .setView(menu).show();
            }

            menu.findViewById(R.id.deleteCommentTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deletePost();
                    dialog.dismiss();
                }
            });

            menu.findViewById(R.id.editCommentTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editPost();
                    dialog.dismiss();
                }
            });
        }
    };

    public void deletePost(){

        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.confirmPostDeleteTitle))
                .setMessage(context.getResources().getString(R.string.confirmPostDeleteMessage))
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference().child("users").child(authorUID)
                                .child("posts").child(key);
                        postReference.removeValue();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void editPost(){

//        Log.i("Post Content", postContent);
        Intent intent = new Intent(context, CreateOrEditActivity.class);
        intent.putExtra("posterUID", authorUID);
        intent.putExtra("postKey", key);
        intent.putExtra("content", postContentTV.getText().toString());
        intent.putExtra("contentType", 1);
        context.startActivity(intent);
    }

}