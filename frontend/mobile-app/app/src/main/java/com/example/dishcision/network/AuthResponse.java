package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName(value = "success", alternate = {"status", "ok"})
    private boolean success;

    @SerializedName(value = "message", alternate = {"msg", "error"})
    private String message;

    @SerializedName(value = "token", alternate = {"accessToken", "access_token", "jwt"})
    private String token;

    @SerializedName("data")
    private AuthData data;

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
        if (token != null && !token.isEmpty()) return token;
        if (data != null && data.accessToken != null) return data.accessToken;
        return "";
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthData getData() {
        return data;
    }

    public void setData(AuthData data) {
        this.data = data;
    }

    public static class AuthData {
        @SerializedName("tokenHash")
        public String tokenHash;

        @SerializedName("testOtp")
        public String testOtp;

        @SerializedName("accessToken")
        public String accessToken;

        @SerializedName("refreshToken")
        public String refreshToken;
    }
}
