package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class TwoFactorVerifyRequest {

    @SerializedName("code")
    private String code;

    public TwoFactorVerifyRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
