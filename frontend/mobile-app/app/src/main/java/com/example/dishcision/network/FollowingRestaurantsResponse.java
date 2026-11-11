package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FollowingRestaurantsResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private RestaurantData data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RestaurantData getData() {
        return data;
    }

    public void setData(RestaurantData data) {
        this.data = data;
    }

    public static class RestaurantData {
        @SerializedName("newDishesSummary")
        public String newDishesSummary;

        @SerializedName("totalFollowing")
        public int totalFollowing;

        @SerializedName("openStoresCount")
        public int openStoresCount;

        @SerializedName("stories")
        public List<StoryItem> stories;

        @SerializedName("restaurants")
        public List<StoreItem> restaurants;
    }

    public static class StoryItem {
        @SerializedName("id")
        public int id;

        @SerializedName("userId")
        public int userId;

        @SerializedName("storeName")
        public String storeName;

        @SerializedName("title")
        public String title;

        @SerializedName("avatarUrl")
        public String avatarUrl;

        @SerializedName("mediaUrl")
        public String mediaUrl;
    }

    public static class StoreItem {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("username")
        public String username;

        @SerializedName("avatarUrl")
        public String avatarUrl;

        @SerializedName("coverUrl")
        public String coverUrl;

        @SerializedName("address")
        public String address;

        @SerializedName("openHours")
        public String openHours;

        @SerializedName("isOpen")
        public boolean isOpen;

        @SerializedName("rating")
        public double rating;

        @SerializedName("reviewsCount")
        public int reviewsCount;

        @SerializedName("followersCount")
        public int followersCount;

        @SerializedName("isFollowing")
        public boolean isFollowing;

        @SerializedName("highlightDish")
        public String highlightDish;

        @SerializedName("dishPrice")
        public String dishPrice;

        @SerializedName("dishDescription")
        public String dishDescription;

        @SerializedName("menu")
        public List<MenuItem> menu;
    }

    public static class MenuItem {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("price")
        public String price;

        @SerializedName("desc")
        public String desc;
    }
}

