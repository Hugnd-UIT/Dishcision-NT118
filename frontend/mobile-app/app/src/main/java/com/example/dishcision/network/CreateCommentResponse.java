package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class CreateCommentResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public CommentsListResponse.CommentItem data;
}
