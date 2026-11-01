package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class PrivacySettingsResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private PrivacySettingsModel data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public PrivacySettingsModel getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
