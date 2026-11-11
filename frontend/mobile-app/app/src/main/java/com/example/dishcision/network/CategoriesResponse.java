package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoriesResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<CategoryItem> data;

    public static class CategoryItem {
        @SerializedName("categoryId")
        public int categoryId;

        @SerializedName("name")
        public String name;

        @SerializedName("icon")
        public String icon;
    }
}

