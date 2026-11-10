package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FeedPostsResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<PostItem> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PostItem> getData() {
        return data;
    }

    public void setData(List<PostItem> data) {
        this.data = data;
    }

    public static class PostItem {
        @SerializedName("postId")
        public int postId;

        @SerializedName("author")
        public Author author;

        @SerializedName("content")
        public String content;

        @SerializedName("locationUrl")
        public String locationUrl;

        @SerializedName("voucherCode")
        public String voucherCode;

        @SerializedName("hashtags")
        public List<String> hashtags;

        @SerializedName("likesCount")
        public int likesCount;

        @SerializedName("commentsCount")
        public int commentsCount;

        @SerializedName("sharesCount")
        public int sharesCount;

        @SerializedName("createdAt")
        public String createdAt;

        @SerializedName("images")
        public List<PostImage> images;

        /** Whether the signed-in user has saved (bookmarked) this post. */
        @SerializedName("isSaved")
        public boolean isSaved;
    }

    public static class Author {
        @SerializedName("userId")
        public int userId;

        @SerializedName("username")
        public String username;

        @SerializedName("fullName")
        public String fullName;

        @SerializedName("avatarUrl")
        public String avatarUrl;

        @SerializedName("bio")
        public String bio;
    }

    public static class PostImage {
        @SerializedName("id")
        public int id;

        @SerializedName("imageUrl")
        public String imageUrl;

        @SerializedName("displayOrder")
        public int displayOrder;
    }
}

