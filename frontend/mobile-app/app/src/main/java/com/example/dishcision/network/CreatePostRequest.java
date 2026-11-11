package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreatePostRequest {

    @SerializedName("content")
    private String content;

    @SerializedName("locationUrl")
    private String locationUrl;

    @SerializedName("voucherCode")
    private String voucherCode;

    @SerializedName("hashtags")
    private List<String> hashtags;

    @SerializedName("images")
    private List<PostImageRequest> images;

    public CreatePostRequest(String content, String locationUrl, String voucherCode, List<String> hashtags, List<PostImageRequest> images) {
        this.content = content;
        this.locationUrl = locationUrl;
        this.voucherCode = voucherCode;
        this.hashtags = hashtags;
        this.images = images;
    }

    public String getContent() {
        return content;
    }

    public String getLocationUrl() {
        return locationUrl;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public List<PostImageRequest> getImages() {
        return images;
    }

    public static class PostImageRequest {
        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("displayOrder")
        private int displayOrder;

        public PostImageRequest(String imageUrl, int displayOrder) {
            this.imageUrl = imageUrl;
            this.displayOrder = displayOrder;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public int getDisplayOrder() {
            return displayOrder;
        }
    }
}
