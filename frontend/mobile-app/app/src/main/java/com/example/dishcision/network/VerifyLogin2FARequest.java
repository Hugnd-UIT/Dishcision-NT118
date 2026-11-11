package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class VerifyLogin2FARequest {

    @SerializedName("tempToken")
    private final String tempToken;

    @SerializedName("otp")
    private final String otp;

    public VerifyLogin2FARequest(String tempToken, String otp) {
        this.tempToken = tempToken;
        this.otp = otp;
    }

    public String getTempToken() {
        return tempToken;
    }

    public String getOtp() {
        return otp;
    }
}

