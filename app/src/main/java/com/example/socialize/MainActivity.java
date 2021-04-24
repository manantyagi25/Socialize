package com.example.socialize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static com.example.socialize.AccountSettingsFragment.profilePic;

public class MainActivity extends AppCompatActivity {

    static public SharedPreferences preferences;
    static public FirebaseAuth auth;
    static public FirebaseApp app;
    static public FirebaseStorage storage;
    static public StorageReference storageReference;
    DatabaseReference onlineStatusReference;
    static public FirebaseUser user;
    public static BottomNavigationView bottomNavigationView;
    private ArrayList<String> friends;

    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private AlertDialog deleteDialog;
    private Fragment selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences("com.example.newlogin", MODE_PRIVATE);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
        else {
            String name = user.getDisplayName();
            preferences.edit().putString("name", name).apply();

            onlineStatusReference = FirebaseDatabase.getInstance().getReference()
                    .child(getResources().getString(R.string.tableName))
                    .child(user.getUid())
                    .child(getResources().getString(R.string.onlineStatus));
            onlineStatusReference.setValue(1);

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            bottomNavigationView = findViewById(R.id.navBar);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int previousItem = bottomNavigationView.getSelectedItemId();
                    int nextItem = item.getItemId();
                    if (previousItem != nextItem) {
                        selected = null;

                        switch (item.getItemId()) {
                            case R.id.nav_home:
                                selected = new HomeFragment();
                                break;

                            case R.id.nav_search:
                                selected = new SearchFragment();
                                break;

                            case R.id.nav_friends:
                                selected = new FriendsFragment();
                                break;

                            case R.id.nav_accountData:
                                selected = new ProfileFragment();
                                break;

                            case R.id.nav_accountSettings:
                                selected = new AccountSettingsFragment();
                                break;
                        }

                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    public void imageChangerDialog(View view) {

        deleteDialog = new AlertDialog.Builder(this , R.style.CustomDialog)
                .setView(R.layout.image_changer_dialog)
                .show();

        //create();
    }

    public void imageChanger(View view) {
        if (view.getTag().toString().equals("gallery")) {
            chooseImageAndUpload();
        } else if (view.getTag().toString().equals("remove")) {
            StorageReference deleteImage = storageReference.child(user.getUid());
            deleteImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    profilePic.setImageResource(R.drawable.fb);
                    File path = new File(getApplicationContext().getExternalFilesDir(null).toString(), "/profilePics/");
                    File pathToProfilePic = new File(path, user.getUid());
                    boolean deleted = pathToProfilePic.delete();
                    if (deleted)
                        Toast.makeText(getApplicationContext(), "Image Deleted Successfully", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "An error occurred!", Toast.LENGTH_SHORT).show();

                }
            });
        }
        else if(view.getTag().toString().equals("camera"))
            Toast.makeText(getApplicationContext(), "Service not available yet!", Toast.LENGTH_SHORT).show();
        deleteDialog.dismiss();
    }

    private void chooseImageAndUpload() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profilePic.setImageBitmap(bitmap);

                if (filePath != null) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();

                    StorageReference ref = storageReference.child(user.getUid());
                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadAndSetUserDP(final ImageView profilePic, final String uid, final Context context) {
        File path = new File(context.getExternalFilesDir(null).toString(), "/profilePics/");
        File pathToProfilePic = new File(path, uid);
        Log.i("Path", pathToProfilePic.toString());
        if (pathToProfilePic.exists()) {
            Log.i("File", "exists");
            Bitmap dp = BitmapFactory.decodeFile(pathToProfilePic.toString());
            Bitmap resized = Bitmap.createScaledBitmap(dp, (int)(dp.getWidth()*0.5), (int)(dp.getHeight()*0.5), true);
            profilePic.setImageBitmap(resized);
        }
        else {
            Log.i("File", "not exists");
            storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(uid).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //                        profilePic.setImageBitmap(uri.getPath());
                            Picasso.get().load(uri).into(profilePic);

                            Target target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap1, Picasso.LoadedFrom from) {

                                    BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
                                    Bitmap bitmap = drawable.getBitmap();
                                    try {
                                        File file = new File(context.getExternalFilesDir(null).toString());
                                        File myDir = new File(file, "profilePics");
                                        if (!myDir.exists())
                                            myDir.mkdirs();

                                        myDir = new File(myDir, uid);
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
                            profilePic.setImageResource(R.drawable.fb);
                            Toast.makeText(context, "Error downloading profile pic!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        /*BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navBar);
        int selected = bottomNavigationView.getSelectedItemId();
        if (selected != R.id.nav_home)
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        else*/
            super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        onlineStatusReference.setValue(0);
        super.onDestroy();
    }
}