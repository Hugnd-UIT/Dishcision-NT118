package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class TwoFactorSetupResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public SetupData data;

    public static class SetupData {
        @SerializedName("secret")
        public String secret;

        @SerializedName("formattedKey")
        public String formattedKey;

        @SerializedName("otpauthUrl")
        public String otpauthUrl;

        @SerializedName("email")
        public String email;

        @SerializedName("emailMasked")
        public String emailMasked;

        @SerializedName("otpSent")
        public boolean otpSent;

        @SerializedName("alreadySent")
        public boolean alreadySent;

        @SerializedName("retryAfterSeconds")
        public int retryAfterSeconds;
    }
}

