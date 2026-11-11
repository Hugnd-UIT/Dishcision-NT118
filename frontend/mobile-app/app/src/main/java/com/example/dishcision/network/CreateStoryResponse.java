package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class CreateStoryResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public StoryModel data;
}

