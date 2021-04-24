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

import static com.example.socialize.MainActivity.bottomNavigationView;
import static com.example.socialize.MainActivity.user;

public class CommentHolder extends RecyclerView.ViewHolder {

    ImageView commentUserDP, likeIcon, optionsMenu;
    TextView commentUserName, commentContent, likeCount;
    private String commentKey, commenterUID, authorUID, postKey;
    public Context context;
    private AlertDialog dialog;

    public CommentHolder(@NonNull View itemView) {
        super(itemView);

        this.commentUserDP = itemView.findViewById(R.id.commentUserDP);
        this.likeIcon = itemView.findViewById(R.id.commentLikeIcon);
        this.commentUserName = itemView.findViewById(R.id.commentUserName);
        this.commentContent = itemView.findViewById(R.id.commentContentTV);
        this.likeCount = itemView.findViewById(R.id.commentLikeCount);
        this.optionsMenu = itemView.findViewById(R.id.optionsMenuIVComment);
        this.context = itemView.getContext();

        commentUserDP.setOnClickListener(listener);
        commentUserName.setOnClickListener(listener);

        likeIcon.setOnClickListener(likeListener);
        optionsMenu.setOnClickListener(menuListener);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.i("Cardview Long Press", "Registered");
                return false;
            }
        });
    }

    public void setKeyAndCommenterUID(String key, final String commenterUID, final String authorUID, final String postKey){
        this.commentKey = key;
        this.commenterUID = commenterUID;
        this.authorUID = authorUID;
        this.postKey = postKey;

        /*if(commenterUID.equals(user.getUid()))
            optionsMenu.setVisibility(View.VISIBLE);
        else
            optionsMenu.setVisibility(View.INVISIBLE);*/

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(authorUID)
                .child("posts").child(postKey).child("comments").child(key).child("likedBy");      // Query to retrieve likedBy list from Firebase

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

        File path = new File(context.getExternalFilesDir(null).toString(), "/profilePics/");
        File pathToProfilePic = new File(path, commenterUID);
        Log.i("Path", pathToProfilePic.toString());
        if (pathToProfilePic.exists()) {
            Log.i("File", "exists");
            Bitmap dp = BitmapFactory.decodeFile(pathToProfilePic.toString());
            Bitmap resized = Bitmap.createScaledBitmap(dp, (int)(dp.getWidth()*0.1), (int)(dp.getHeight()*0.1), true);
            commentUserDP.setImageBitmap(resized);
        }
        else {
            Log.i("File", "not exists");
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(key).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //                        profilePic.setImageBitmap(uri.getPath());
                            Picasso.get().load(uri).into(commentUserDP);

                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap1, Picasso.LoadedFrom from) {

                                    BitmapDrawable drawable = (BitmapDrawable) commentUserDP.getDrawable();
                                    Bitmap bitmap = drawable.getBitmap();
                                    try {
                                        File file = new File(context.getExternalFilesDir(null).toString());
                                        File myDir = new File(file, "profilePics");
                                        if (!myDir.exists())
                                            myDir.mkdirs();

                                        myDir = new File(myDir, commenterUID);
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
                            commentUserDP.setImageResource(R.drawable.socialize);
                            Toast.makeText(context, "Error downloading profile pic!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            if (!commenterUID.equals(user.getUid())) {
                ViewAccountFragment viewAccountFragment = new ViewAccountFragment();
                Bundle args = new Bundle();
                args.putString(viewAccountFragment.UID, commenterUID);
                viewAccountFragment.setArguments(args);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, viewAccountFragment)
                        .addToBackStack(null).commit();
            }
            else {
                bottomNavigationView.setSelectedItemId(R.id.nav_accountData);
            }
        }
    };

    View.OnClickListener likeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(authorUID)
                    .child("posts").child(postKey).child("comments").child(commentKey).child("likedBy");      // Query to retrieve likedBy list from Firebase

            Log.i("Reference", reference.toString());

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Log.i("Liked By", "Exists");
                        LikedBy likedBy = snapshot.getValue(LikedBy.class);                 //getting likedBy arraylist
                        final DatabaseReference likesReference = FirebaseDatabase.getInstance().getReference().child("users").child(authorUID)
                                .child("posts").child(postKey).child("comments").child(commentKey).child("likes");   // Reference to likesCount in firebase


                        if (likedBy.checkAuthorLike(user.getUid())) {       //checking if author has liked the post
                            likedBy.removeFromList(user.getUid());          //removing author like
                            likeIcon.setImageResource(R.drawable.like);
                            int likesCount = Integer.parseInt(likeCount.getText().toString());
                            likeCount.setText(String.valueOf(--likesCount));
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
                            int likesCount = Integer.parseInt(likeCount.getText().toString());
                            likeCount.setText(String.valueOf(++likesCount));
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
                    else {
                        Log.i("Liked By", "Does not exist");
                        LikedBy likedBy = new LikedBy();
                        likedBy.addToList(user.getUid());
                        likeIcon.setImageResource(R.drawable.liked);
                        int likesCount = Integer.parseInt(likeCount.getText().toString());
                        likeCount.setText(String.valueOf(++likesCount));
                        reference.setValue(likedBy);
                        DatabaseReference likesReference = FirebaseDatabase.getInstance().getReference().child("users").child(authorUID)
                                .child("posts").child(postKey).child("comments").child(commentKey).child("likes");
                        likesReference.setValue(1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };

    View.OnClickListener menuListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i("CommenterUID", commenterUID);
            LayoutInflater inflater = LayoutInflater.from(context);
            View menu = inflater.inflate(R.layout.owner_menu_layout_comments, null, false);
            if(commenterUID.equals(user.getUid()))
                dialog = new AlertDialog.Builder(view.getContext()).setView(menu).show();

            menu.findViewById(R.id.deleteCommentTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteComment();
                    dialog.dismiss();
                }
            });

            menu.findViewById(R.id.editCommentTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editComment();
                    dialog.dismiss();
                }
            });
        }
    };

    public void deleteComment(){

        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.confirmCommentDeleteTitle))
                .setMessage(context.getResources().getString(R.string.confirmCommentDeleteMessage))
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                        DatabaseReference commentReference = FirebaseDatabase.getInstance().getReference().child(context.getResources().getString(R.string.tableName))
                                .child(authorUID).child(context.getResources().getString(R.string.posts)).child(postKey)
                                .child(context.getResources().getString(R.string.comments)).child(commentKey);
                        commentReference.removeValue();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void editComment(){

        Intent intent = new Intent(context, CreateOrEditActivity.class);
        intent.putExtra("posterUID", authorUID);
        intent.putExtra("postKey", postKey);
        intent.putExtra("commenterUID", commenterUID);
        intent.putExtra("commentKey", commentKey);
        intent.putExtra("content", commentContent.getText().toString());
        intent.putExtra("contentType", 2);
        context.startActivity(intent);
    }
}

