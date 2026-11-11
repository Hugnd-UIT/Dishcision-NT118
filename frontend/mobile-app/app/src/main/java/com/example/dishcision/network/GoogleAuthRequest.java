package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class GoogleAuthRequest {

    @SerializedName("email")
    public String email;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("avatarUrl")
    public String avatarUrl;

    public GoogleAuthRequest(String email, String fullName, String avatarUrl) {
        this.email = email;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }
}

