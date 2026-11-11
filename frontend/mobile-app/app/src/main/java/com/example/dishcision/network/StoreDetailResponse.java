package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StoreDetailResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public StoreDetailData data;

    public static class StoreDetailData {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("username")
        public String username;

        @SerializedName("bio")
        public String bio;

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

        @SerializedName("highlightDish")
        public String highlightDish;

        @SerializedName("dishPrice")
        public String dishPrice;

        @SerializedName("dishDescription")
        public String dishDescription;

        @SerializedName("isFollowing")
        public boolean isFollowing;

        @SerializedName("followersCount")
        public int followersCount;

        @SerializedName("reviewsCount")
        public int reviewsCount;

        @SerializedName("reviews")
        public List<ReviewsResponse.ReviewItem> reviews;

        @SerializedName("menu")
        public List<MenuDishItem> menu;

        @SerializedName("posts")
        public List<FeedPostsResponse.PostItem> posts;
    }

    public static class MenuDishItem {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("price")
        public String price;

        @SerializedName("priceInt")
        public int priceInt;

        @SerializedName("desc")
        public String desc;

        @SerializedName("description")
        public String description;

        @SerializedName("imageUrl")
        public String imageUrl;

        @SerializedName("category")
        public String category;

        @SerializedName("rating")
        public double rating;
    }
}

