package com.example.socialize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.socialize.MainActivity.auth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    SharedPreferences preferences;
    CheckBox keepSignedIn;

    public void startSignUpActivity(View view){
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    }

    public void validateInputAndLoginUser(final View view){
         {
            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {                              //Sign in Successful

                        if(keepSignedIn.isChecked()){
                            preferences.edit().putString("username", email.getText().toString()).apply();
                            preferences.edit().putString("password", password.getText().toString()).apply();
                            preferences.edit().putBoolean("keepSignedIn", keepSignedIn.isChecked()).apply();
                        }
                        else {
                            preferences.edit().putString("username", "").apply();
                            preferences.edit().putString("password", "").apply();
                            preferences.edit().putBoolean("keepSignedIn", false).apply();
                        }

//                        view.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else                                                //Sign in Failure
                        Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        preferences = getSharedPreferences("com.example.newlogin", MODE_PRIVATE);
        keepSignedIn = findViewById(R.id.keepSignedIn);

        if(preferences.getBoolean("keepSignedIn", false)){
            email.setText(preferences.getString("username", ""));
            password.setText(preferences.getString("password",  ""));
            keepSignedIn.setChecked(preferences.getBoolean("keepSignedIn", false));
        }

        auth = FirebaseAuth.getInstance();
    }
}