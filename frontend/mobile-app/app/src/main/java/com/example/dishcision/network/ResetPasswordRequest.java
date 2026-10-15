package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {

    @SerializedName("tokenHash")
    private String tokenHash;

    @SerializedName("newPassword")
    private String newPassword;

    public ResetPasswordRequest(String tokenHash, String newPassword) {
        this.tokenHash = tokenHash;
        this.newPassword = newPassword;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
