package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class DishModel {
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
}

