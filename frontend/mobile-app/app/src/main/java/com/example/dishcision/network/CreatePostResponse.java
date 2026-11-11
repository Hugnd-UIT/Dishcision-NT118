package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class CreatePostResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private FeedPostsResponse.PostItem data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public FeedPostsResponse.PostItem getData() {
        return data;
    }
}
