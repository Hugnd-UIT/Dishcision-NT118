package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DishDetailResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public DishDetailData data;

    public static class DishDetailData {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("price")
        public int price;

        @SerializedName("formattedPrice")
        public String formattedPrice;

        @SerializedName("description")
        public String description;

        @SerializedName("imageUrl")
        public String imageUrl;

        @SerializedName("category")
        public String category;

        @SerializedName("rating")
        public double rating;

        @SerializedName("reviewsCount")
        public int reviewsCount;

        @SerializedName("storeId")
        public int storeId;

        @SerializedName("storeName")
        public String storeName;

        @SerializedName("storeAddress")
        public String storeAddress;

        @SerializedName("storeRating")
        public double storeRating;

        @SerializedName("storeOpenHours")
        public String storeOpenHours;

        @SerializedName("storeReviewsCount")
        public int storeReviewsCount;

        @SerializedName("reviews")
        public List<DishReviewItem> reviews;
    }

    public static class DishReviewItem {
        @SerializedName("reviewId")
        public int reviewId;

        @SerializedName("rating")
        public int rating;

        @SerializedName("content")
        public String content;

        @SerializedName("authorName")
        public String authorName;

        @SerializedName("createdAt")
        public String createdAt;
    }
}

