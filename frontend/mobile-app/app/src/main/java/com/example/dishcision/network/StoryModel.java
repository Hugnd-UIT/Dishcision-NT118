package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class StoryModel {
    @SerializedName("id")
    public int id;

    @SerializedName("userId")
    public int userId;

    @SerializedName("username")
    public String username;

    @SerializedName("userFullName")
    public String userFullName;

    @SerializedName("userAvatar")
    public String userAvatar;

    @SerializedName("mediaUrl")
    public String mediaUrl;

    @SerializedName("caption")
    public String caption;

    @SerializedName("createdAt")
    public String createdAt;
}

