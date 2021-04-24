package com.example.socialize;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import static com.example.socialize.MainActivity.user;

public class FriendsLVHolder extends RecyclerView.ViewHolder {

    ImageView dp;
    TextView name;
    private String key;
    private Context context;
    private boolean fromSearch = true;


    public FriendsLVHolder(@NonNull View itemView) {
        super(itemView);

        this.dp = itemView.findViewById(R.id.userDP);
        this.name = itemView.findViewById(R.id.friendName);
        this.context = itemView.getContext();

        dp.setOnClickListener(onClickListener);
        name.setOnClickListener(onClickListener);
    }

    public void setKey(String uid, boolean fromSearch){
        this.key = uid;
        this.fromSearch = fromSearch;

        if (!fromSearch)
            name.setOnLongClickListener(longClickListener);

        downloadAndSetDP();
    }

    private void downloadAndSetDP(){
        File path = new File(context.getExternalFilesDir(null).toString(), "/profilePics/");
        File pathToProfilePic = new File(path, key);
        Log.i("Path", pathToProfilePic.toString());
        if (pathToProfilePic.exists()) {
            Bitmap image = BitmapFactory.decodeFile(pathToProfilePic.toString());
            Bitmap resized = Bitmap.createScaledBitmap(image, (int)(image.getWidth()*0.1), (int)(image.getHeight()*0.1), true);
            dp.setImageBitmap(resized);
        }
        else {
            Log.i("File", "not exists");
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(key).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //                        profilePic.setImageBitmap(uri.getPath());
                            Picasso.get().load(uri).into(dp);

                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap1, Picasso.LoadedFrom from) {

                                    BitmapDrawable drawable = (BitmapDrawable) dp.getDrawable();
                                    Bitmap bitmap = drawable.getBitmap();
                                    try {
                                        File file = new File(context.getExternalFilesDir(null).toString());
                                        File myDir = new File(file, "profilePics");
                                        if (!myDir.exists())
                                            myDir.mkdirs();

                                        myDir = new File(myDir, key);
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
                            dp.setImageResource(R.drawable.socialize);
                            Toast.makeText(context, "Error downloading profile pic!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewAccountFragment viewAccountFragment = new ViewAccountFragment();
            Bundle args = new Bundle();
            args.putString(ViewAccountFragment.UID, key);
            viewAccountFragment.setArguments(args);
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, viewAccountFragment).addToBackStack(null).commit();
        }
    };

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View view) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Remove Friend?")
                    .setMessage("You can always add them as friend later")
                    .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Toast.makeText(view.getContext(), name.getText().toString() + " removed from your Friends", Toast.LENGTH_SHORT).show();

                            final DatabaseReference userFriendListReference = FirebaseDatabase.getInstance().getReference()
                                    .child(view.getResources().getString(R.string.tableName))
                                    .child(user.getUid())
                                    .child(view.getResources().getString(R.string.friends));
                            userFriendListReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    Log.i("Friends List Snapshot", snapshot.toString());
                                    Friends friends = snapshot.getValue(Friends.class);
                                    friends.removeFriendFromList(key);
                                    userFriendListReference.setValue(friends);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            final DatabaseReference keyFriendListReference = FirebaseDatabase.getInstance().getReference()
                                    .child(view.getResources().getString(R.string.tableName))
                                    .child(key)
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

            return false;
        }
    };
}
