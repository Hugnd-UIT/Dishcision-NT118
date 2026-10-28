package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HelpInboxResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private HelpInboxData data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public HelpInboxData getData() { return data; }
    public String getMessage() { return message; }

    public static class HelpInboxData {
        @SerializedName("items")
        private List<HelpItem> items;

        public List<HelpItem> getItems() { return items; }
    }

    public static class HelpItem {
        @SerializedName("id")
        private String id;
        @SerializedName("type")
        private String type;
        @SerializedName("title")
        private String title;
        @SerializedName("subtitle")
        private String subtitle;
        @SerializedName("date")
        private String date;
        @SerializedName("status")
        private String status;

        public String getId() { return id; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
    }
}