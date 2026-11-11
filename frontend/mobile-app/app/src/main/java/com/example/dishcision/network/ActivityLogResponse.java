package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ActivityLogResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<ActivityItem> data;

    public static class ActivityItem {
        @SerializedName("id")
        public String id;

        @SerializedName("type")
        public String type;

        @SerializedName("targetId")
        public int targetId;

        @SerializedName("title")
        public String title;

        @SerializedName("snippet")
        public String snippet;

        @SerializedName("createdAt")
        public String createdAt;
    }
}

