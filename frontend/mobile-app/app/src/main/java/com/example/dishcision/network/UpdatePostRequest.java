package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UpdatePostRequest {

    @SerializedName("content")
    public String content;

    @SerializedName("locationUrl")
    public String locationUrl;

    @SerializedName("voucherCode")
    public String voucherCode;

    @SerializedName("hashtags")
    public List<String> hashtags;

    @SerializedName("status")
    public String status;
}

