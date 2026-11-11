package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class AddressRequest {
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

    public AddressRequest(String label, String recipientName, String phoneNumber, String detailAddress, String notes, boolean isDefault) {
        this.label = label;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.detailAddress = detailAddress;
        this.notes = notes;
        this.isDefault = isDefault;
    }
}

