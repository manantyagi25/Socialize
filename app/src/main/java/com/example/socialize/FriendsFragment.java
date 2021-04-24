package com.example.socialize;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.socialize.MainActivity.bottomNavigationView;
import static com.example.socialize.MainActivity.user;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {

    Button findNewFriends;
    RecyclerView friendsRV;
    ArrayList<String> userFriends, friendsKey;
    private ArrayList<String> friendUIDs;
    public FriendsLVAdapter friendsLVAdapter;
    ConstraintLayout loadingProgress;
    TextView noFriendsFoundTV;
    ImageView chatZone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userFriends = new ArrayList<>();
        friendsKey = new ArrayList<>();
        friendsLVAdapter = new FriendsLVAdapter(getContext(), userFriends, friendsKey, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findNewFriends = view.findViewById(R.id.findNewFriends);
        friendsRV = view.findViewById(R.id.friendsRV);
        loadingProgress = view.findViewById(R.id.progressBarCL);
        noFriendsFoundTV = view.findViewById(R.id.noFriendsFoundTV);
        chatZone = view.findViewById(R.id.chatZoneButton);

        findNewFriends.setOnClickListener(findNewFriendsListener);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        friendsRV.setLayoutManager(manager);
        friendsRV.setAdapter(friendsLVAdapter);

        userFriends.clear();
        friendsKey.clear();

        DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("friends");
        friendsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Friends friends = snapshot.getValue(Friends.class);
                    friendUIDs = friends.getFriendIDs();

                    for (String uid : friendUIDs){
                        friendsKey.add(uid);
                        DatabaseReference friendData = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                        friendData.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                String name = user.getFirstName() + " " + user.getLastName();
                                userFriends.add(name);
                                friendsLVAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                else {
                    noFriendsFoundTV.setVisibility(View.VISIBLE);
                }
                loadingProgress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatZone.setOnClickListener(chatZoneListener);
    }

    View.OnClickListener findNewFriendsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bottomNavigationView.setSelectedItemId(R.id.nav_search);
        }
    };

    View.OnClickListener chatZoneListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            if(isAppInstalled(context)){
                Intent intent = context.getPackageManager()
                        .getLaunchIntentForPackage(getResources().getString(R.string.chatZonePackageName));

                if(intent != null){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
            else {
                Toast.makeText(context, getResources().getString(R.string.chatZoneNotFound), Toast.LENGTH_LONG).show();
            }
        }
    };

    private boolean isAppInstalled(Context context){
        PackageManager manager = context.getPackageManager();
        try{
            manager.getPackageInfo(getResources().getString(R.string.chatZonePackageName), PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
