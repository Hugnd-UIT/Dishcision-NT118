package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewsResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<ReviewItem> data;

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

    public List<ReviewItem> getData() {
        return data;
    }

    public void setData(List<ReviewItem> data) {
        this.data = data;
    }

    public static class ReviewItem {
        @SerializedName("reviewId")
        public int reviewId;

        @SerializedName("restaurantName")
        public String restaurantName;

        @SerializedName("locationUrl")
        public String locationUrl;

        @SerializedName("rating")
        public int rating;

        @SerializedName("content")
        public String content;

        @SerializedName("tags")
        public List<String> tags;

        @SerializedName("createdAt")
        public String createdAt;

        @SerializedName("author")
        public FeedPostsResponse.Author author;
    }
}

