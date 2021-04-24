package com.example.socialize;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.socialize.MainActivity.user;

public class SearchFragment extends Fragment {

//    ListView searchList;
    RecyclerView friendSearchRV;
    ArrayList<String> foundUsers;
    ArrayList<String> names;
//    ArrayAdapter<String> adapter;
    EditText searchET;
//    Button searchFriendsButton;
    FriendsLVAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        foundUsers = new ArrayList<>();
        names = new ArrayList<>();
//        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, names);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        searchFriendsButton = view.findViewById(R.id.searchFriendsButton);
        searchET = view.findViewById(R.id.searchET);
//        searchList = view.findViewById(R.id.searchLV);
        friendSearchRV = view.findViewById(R.id.searchRV);

//        searchList.setAdapter(adapter);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        friendSearchRV.setLayoutManager(manager);
        adapter = new FriendsLVAdapter(getContext(), names, foundUsers, true);
        friendSearchRV.setAdapter(adapter);

        searchET.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                foundUsers.clear();
                names.clear();
                adapter.notifyDataSetChanged();

                if(searchET.getText().toString().length() != 0){
                    queryUsers(searchET.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        /*searchFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foundUsers.clear();
                names.clear();
                adapter.notifyDataSetChanged();

                String entered = searchET.getText().toString().toLowerCase();
                if(entered.length() > 0) {
                    queryUsers(entered);
//                    friendNameSearchQuery = FirebaseDatabase.getInstance().getReference()
                }
                else {
                    names.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });*/

    }




    private void queryUsers(final String entered) {

        Query userNameSearchQuery = FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.tableName)).limitToFirst(50);
        userNameSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = dataSnapshot.child("fullName").getValue().toString();

                    if (!name.equals(user.getDisplayName())) {

                        Pattern pattern = Pattern.compile(entered, Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(name);
                        boolean isFound = matcher.find();
                        if (isFound) {
                            names.add(name);
                            foundUsers.add(dataSnapshot.getKey());
                            Log.i("User", name);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
