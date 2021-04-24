package com.example.socialize;

import java.util.ArrayList;

public class Friends {

    public ArrayList<String> friendIDs = new ArrayList<>();

    public ArrayList<String> getFriendIDs() {
        return friendIDs;
    }

    public void setFriendIDs(ArrayList<String> friendIDs) {
        this.friendIDs = friendIDs;
    }

    public Friends() {
    }

    public Friends(ArrayList<String> friendIDs) {
        this.friendIDs = friendIDs;
    }

    public void addFriendToList(String uid){
        friendIDs.add(uid);
    }

    public void removeFriendFromList(String uid){
        friendIDs.remove(uid);
    }

    public boolean checkIfUserExistsInFriendsList(String uid){
        if(friendIDs.contains(uid))
            return true;
        else
            return false;
    }
}
