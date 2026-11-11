package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class ResendLogin2FARequest {

    @SerializedName("tempToken")
    private final String tempToken;

    public ResendLogin2FARequest(String tempToken) {
        this.tempToken = tempToken;
    }

    public String getTempToken() {
        return tempToken;
    }
}

