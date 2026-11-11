package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchStoresResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<StoreSearchItem> data;

    public static class StoreSearchItem {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("username")
        public String username;

        @SerializedName("avatarUrl")
        public String avatarUrl;

        @SerializedName("bio")
        public String bio;

        @SerializedName("address")
        public String address;

        @SerializedName("openHours")
        public String openHours;

        @SerializedName("isOpen")
        public boolean isOpen;

        @SerializedName("rating")
        public double rating;

        @SerializedName("highlightDish")
        public String highlightDish;

        @SerializedName("dishPrice")
        public String dishPrice;

        @SerializedName("followersCount")
        public int followersCount;
    }
}

