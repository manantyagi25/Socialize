package com.example.socialize;

import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;

import java.util.ArrayList;

public class LikedBy {

    ArrayList<String> likedBy = new ArrayList<>();

    public LikedBy(ArrayList<String> likedBy) {
        this.likedBy = likedBy;
    }

    public LikedBy(){
    }

    public ArrayList<String> getlikedBy() {
        return likedBy;
    }

    public void setlikedBy(ArrayList<String> likedBy) {
        this.likedBy = likedBy;
    }

    public void addToList(String userID){
        likedBy.add(userID);
    }

    public void removeFromList(String uid){
        likedBy.remove(uid);
    }

    public boolean checkAuthorLike(String uid){
        if(likedBy.contains(uid))
            return true;
        else
            return false;
    }
}
