package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserPreferencesResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<PreferenceItem> data;

    public static class PreferenceItem {
        @SerializedName("preferenceId")
        public int preferenceId;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("icon")
        public String icon;
    }
}

