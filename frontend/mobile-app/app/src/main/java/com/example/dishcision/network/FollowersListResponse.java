package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FollowersListResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<FollowItem> data;

    public static class FollowItem {
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

        @SerializedName("isFollowing")
        public boolean isFollowing;
    }
}
