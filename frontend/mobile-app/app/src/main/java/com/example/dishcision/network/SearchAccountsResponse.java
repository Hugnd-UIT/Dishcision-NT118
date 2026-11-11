package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchAccountsResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<FeedPostsResponse.Author> data;
}

