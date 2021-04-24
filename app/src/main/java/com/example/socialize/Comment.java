package com.example.socialize;

public class Comment {

    public String commentText;
    public String commentUserName;
    public String commenterUID;
    public int likes;
    public LikedBy likedBy;

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentUserName() {
        return commentUserName;
    }

    public void setCommentUserName(String commentUserName) {
        this.commentUserName = commentUserName;
    }

    public String getCommenterUID() {
        return commenterUID;
    }

    public void setCommenterUID(String commenterUID) {
        this.commenterUID = commenterUID;
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

    public Comment(String commentText, String commentUserName, String commenterUID, int likes, LikedBy likedBy) {
        this.commentText = commentText;
        this.commentUserName = commentUserName;
        this.commenterUID = commenterUID;
        this.likes = likes;
        this.likedBy = likedBy;
    }

    public Comment(){}

    @Override
    public String toString() {
        return "Comment{" +
                "commentText='" + commentText + '\'' +
                ", commentUserName='" + commentUserName + '\'' +
                ", commenterUID='" + commenterUID + '\'' +
                ", likes=" + likes +
                ", likedBy=" + likedBy +
                '}';
    }
}
