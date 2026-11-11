package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class RandomWheelResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public RandomWheelData data;

    public static class RandomWheelData {
        @SerializedName("dishId")
        public int dishId;

        @SerializedName("dishName")
        public String dishName;

        @SerializedName("imageUrl")
        public String imageUrl;

        @SerializedName("category")
        public String category;

        @SerializedName("storeId")
        public int storeId;

        @SerializedName("restaurant")
        public RestaurantInfo restaurant;
    }

    public static class RestaurantInfo {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("address")
        public String address;

        @SerializedName("mapsUrl")
        public String mapsUrl;

        @SerializedName("estimatedPrice")
        public int estimatedPrice;

        @SerializedName("formattedPrice")
        public String formattedPrice;

        @SerializedName("rating")
        public double rating;
    }
}

