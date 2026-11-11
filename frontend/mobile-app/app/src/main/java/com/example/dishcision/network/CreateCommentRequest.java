package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class CreateCommentRequest {

    @SerializedName("content")
    private String content;

    @SerializedName("parentCommentId")
    private Integer parentCommentId;

    public CreateCommentRequest(String content) {
        this.content = content;
        this.parentCommentId = null;
    }

    public CreateCommentRequest(String content, Integer parentCommentId) {
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public Integer getParentCommentId() {
        return parentCommentId;
    }
}
