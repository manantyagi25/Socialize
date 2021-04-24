package com.example.socialize;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import static com.example.socialize.MainActivity.auth;
import static com.example.socialize.MainActivity.downloadAndSetUserDP;
import static com.example.socialize.MainActivity.preferences;
import static com.example.socialize.MainActivity.user;

public class AccountSettingsFragment extends Fragment {

    TextView nameTV;
    ListView accountSettingsLV;
    public static ImageView profilePic;
    ArrayList<String> settings;
    ArrayAdapter<String> adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accountsettings, container, false);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new ArrayList<>();
        settings.add("Update your account data");
        settings.add("Logout");

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, settings);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTV = view.findViewById(R.id.nameTV);
        nameTV.setText(preferences.getString("name", ""));

        accountSettingsLV = view.findViewById(R.id.accountSettingsLV);
        accountSettingsLV.setAdapter(adapter);
        accountSettingsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                doAction(i);
            }
        });

        profilePic = view.findViewById(R.id.profilePicIV);
        profilePic.setImageResource(R.drawable.fb);

        downloadAndSetUserDP(profilePic, user.getUid(), getContext());
    }

    public void logOutUser(){

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm logout?")
                .setMessage("This would sign you out of this device until you log in back")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        auth.getInstance().signOut();
                        Intent intent = new Intent(getContext(), LoginActivity.class);

                        //To make sure going back is not possible
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doAction(int i){
        if(i ==0)
            Toast.makeText(getContext(), "This service is not available now", Toast.LENGTH_SHORT).show();
        else if(i == 1)
            logOutUser();
    }
}
