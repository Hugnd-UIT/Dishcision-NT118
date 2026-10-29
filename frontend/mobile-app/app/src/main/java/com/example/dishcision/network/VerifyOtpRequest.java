package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("otp")
    private String otp;

    public VerifyOtpRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

