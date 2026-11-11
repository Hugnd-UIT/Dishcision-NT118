package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserProfileResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private UserData data;

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

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }

    public static class UserData {
        @SerializedName("userId")
        public int userId;

        @SerializedName("username")
        public String username;

        @SerializedName("email")
        public String email;

        @SerializedName("fullName")
        public String fullName;

        @SerializedName("bio")
        public String bio;

        @SerializedName("city")
        public String city;

        @SerializedName("role")
        public String role;

        @SerializedName("avatarUrl")
        public String avatarUrl;

        @SerializedName("preferences")
        public List<String> preferences;

        @SerializedName("allergies")
        public List<String> allergies;

        @SerializedName("profileRestricted")
        public boolean profileRestricted;

        @SerializedName("postsCount")
        public int postsCount;

        @SerializedName("followersCount")
        public int followersCount;

        @SerializedName("followingCount")
        public int followingCount;

        @SerializedName("isGoogleAccount")
        public boolean isGoogleAccount;

        @SerializedName("isFollowing")
        public boolean isFollowing;
    }
}

