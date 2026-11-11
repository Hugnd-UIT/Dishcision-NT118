package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TrendingResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public TrendingData data;

    public static class TrendingData {
        @SerializedName("trendingHashtags")
        public List<HashtagItem> trendingHashtags;

        @SerializedName("trendingDishes")
        public List<String> trendingDishes;
    }

    public static class HashtagItem {
        @SerializedName("tag")
        public String tag;

        @SerializedName("postCount")
        public int postCount;
    }
}

