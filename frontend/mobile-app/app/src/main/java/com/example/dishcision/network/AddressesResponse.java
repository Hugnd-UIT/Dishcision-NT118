package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AddressesResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<AddressModel> data;
}

