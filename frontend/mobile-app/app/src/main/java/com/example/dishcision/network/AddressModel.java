package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class AddressModel {
    @SerializedName("id")
    public int id;

    @SerializedName("userId")
    public int userId;

    @SerializedName("label")
    public String label;

    @SerializedName("recipientName")
    public String recipientName;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("detailAddress")
    public String detailAddress;

    @SerializedName("notes")
    public String notes;

    @SerializedName("isDefault")
    public boolean isDefault;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;
}

