package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class CreateStoryRequest {
    @SerializedName("mediaUrl")
    public String mediaUrl;

    @SerializedName("caption")
    public String caption;

    public CreateStoryRequest(String mediaUrl, String caption) {
        this.mediaUrl = mediaUrl;
        this.caption = caption;
    }
}

