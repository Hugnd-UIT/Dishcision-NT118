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
    }
}

