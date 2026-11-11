package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class AddressResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public AddressModel data;
}

