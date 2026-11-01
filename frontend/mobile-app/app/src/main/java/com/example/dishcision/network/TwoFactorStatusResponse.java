package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class TwoFactorStatusResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("isTwoFactorEnabled")
    private boolean isTwoFactorEnabled;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public boolean isTwoFactorEnabled() {
        return isTwoFactorEnabled;
    }

    public String getMessage() {
        return message;
    }
}
