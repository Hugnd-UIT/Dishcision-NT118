package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<NotificationItem> data;

    public static class NotificationItem {
        @SerializedName("notificationId")
        public int notificationId;

        @SerializedName("userId")
        public int userId;

        @SerializedName("type")
        public String type;

        @SerializedName("message")
        public String message;

        @SerializedName("isRead")
        public boolean isRead;

        @SerializedName("relatedPostId")
        public Integer relatedPostId;

        @SerializedName("createdAt")
        public String createdAt;
    }
}

