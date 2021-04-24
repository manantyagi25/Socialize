package com.example.socialize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.socialize.MainActivity.user;

public class CreateOrEditActivity extends AppCompatActivity {

    EditText editContentET;
    TextView headingTV;
    Button updateButton, cancelButton;
    String posterUID, postKey, commenterUID, commentKey, currentContent;
    int contentType;        // 0 - create post, 1 - edit post, 2- edit comment
    public static String CONTENT_TYPE = "contentType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editContentET = findViewById(R.id.editContentET);
        headingTV = findViewById(R.id.headingTV);
        updateButton = findViewById(R.id.updateButton);
        cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(cancelListener);

        Intent intent = getIntent();
        contentType = intent.getIntExtra(CONTENT_TYPE, 0);

        if(contentType == 0){
            headingTV.setText(R.string.createNewPost);
            updateButton.setText(R.string.create);
            updateButton.setOnClickListener(createPostListener);
        }
        else {
            currentContent = intent.getStringExtra("content");
            editContentET.setText(currentContent);
            editContentET.setSelection(editContentET.getText().length());
            updateButton.setText(R.string.update);
            updateButton.setOnClickListener(editContentListener);

            if(contentType == 1) {
                headingTV.setText(R.string.editPostContent);
                posterUID = intent.getStringExtra("posterUID");
                postKey = intent.getStringExtra("postKey");
            }
            else {
                headingTV.setText(R.string.editCommentContent);
                posterUID = intent.getStringExtra("posterUID");
                postKey = intent.getStringExtra("postKey");
                commenterUID = intent.getStringExtra("commenterUID");
                commentKey = intent.getStringExtra("commentKey");
            }
        }
    }




    //Listeners

    private View.OnClickListener createPostListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            createPost();
        }
    };

    private View.OnClickListener editContentListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveEdit();
        }
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(contentType == 0 && editContentET.getText().length() == 0)
                finish();
            else
                cancelEdit();
        }
    };



    //Functions to create or edit content

    private void createPost(){
        String postText = editContentET.getText().toString();

        if (postText.length() == 0)
            Toast.makeText(getApplicationContext(), "Can't share empty posts!", Toast.LENGTH_SHORT).show();
        else {
            LikedBy likedBy = new LikedBy();
            likedBy.addToList(user.getUid());
            Posts posts = new Posts(postText, user.getDisplayName(), 1, user.getUid(), likedBy);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child(getResources().getString(R.string.tableName))
                    .child(user.getUid())
                    .child("posts").push();

            reference.setValue(posts)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.postCreatedToastMessage), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            finish();
        }
    }

    private void saveEdit(){

        String updatedContent = editContentET.getText().toString();

        if (contentType == 1){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.postUpdatedToastMessage), Toast.LENGTH_SHORT).show();
            DatabaseReference postReference = FirebaseDatabase.getInstance().getReference()
                    .child(getResources().getString(R.string.tableName))
                    .child(posterUID)
                    .child(getResources().getString(R.string.posts))
                    .child(postKey)
                    .child(getResources().getString(R.string.postText));

            postReference.setValue(updatedContent);
        }
        else if(contentType == 2){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.commentUpdatedToastMessage), Toast.LENGTH_SHORT).show();
            DatabaseReference commentReference = FirebaseDatabase.getInstance().getReference()
                    .child(getResources().getString(R.string.tableName))
                    .child(posterUID)
                    .child(getResources().getString(R.string.posts))
                    .child(postKey)
                    .child(getResources().getString(R.string.comments))
                    .child(commentKey)
                    .child(getResources().getString(R.string.commentText));

            commentReference.setValue(updatedContent);
        }

        finish();
    }

    private void cancelEdit(){

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.cancelEditTitle))
                .setMessage(getResources().getString(R.string.cancelEditMessage))
                .setPositiveButton("Discard Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}