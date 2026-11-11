package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class SendChatMessageRequest {
    @SerializedName("content")
    public String content;

    @SerializedName("mediaUrl")
    public String mediaUrl;

    public SendChatMessageRequest(String content, String mediaUrl) {
        this.content = content;
        this.mediaUrl = mediaUrl;
    }
}

