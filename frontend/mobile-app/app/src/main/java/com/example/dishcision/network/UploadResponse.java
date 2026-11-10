package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public UploadData data;

    public static class UploadData {
        @SerializedName("filename")
        public String filename;

        @SerializedName("url")
        public String url;

        @SerializedName("fullUrl")
        public String fullUrl;
    }
}
