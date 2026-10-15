package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName(value = "success", alternate = {"status", "ok"})
    private boolean success;

    @SerializedName(value = "message", alternate = {"msg", "error"})
    private String message;

    @SerializedName(value = "token", alternate = {"accessToken", "access_token", "jwt"})
    private String token;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
