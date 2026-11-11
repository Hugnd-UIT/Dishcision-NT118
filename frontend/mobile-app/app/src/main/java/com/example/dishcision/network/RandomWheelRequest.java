package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class RandomWheelRequest {
    @SerializedName("budget")
    public String budget;

    @SerializedName("category")
    public String category;

    public RandomWheelRequest(String budget, String category) {
        this.budget = budget;
        this.category = category;
    }
}

