package com.example.socialize;

import com.google.firebase.database.ServerValue;

import java.util.Map;

public class Posts {

    public String postText;
    public String postBy;
    public String authorUID;
    public int likes;
    public LikedBy likedBy;
    //public Map<String, Object> date;

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getPostBy() {
        return postBy;
    }

    public void setPostBy(String postBy) {
        this.postBy = postBy;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public LikedBy getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(LikedBy likedBy) {
        this.likedBy = likedBy;
    }

    public String getAuthorUID() {
        return authorUID;
    }

    public void setAuthorUID(String authorUID) {
        this.authorUID = authorUID;
    }

    public Posts(){

    }

    public Posts(String postText, String postBy, int likes, String authorUID, LikedBy likedBy) {
        this.postText = postText;
        this.postBy = postBy;
        this.likes = likes;
        this.authorUID = authorUID;
        this.likedBy = likedBy;
        //this.date = ServerValue.TIMESTAMP;
    }
}
