package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class FoodDecideRequest {

    @SerializedName("mood")
    private String mood;

    @SerializedName("budget")
    private String budget;

    @SerializedName("companions")
    private int companions;

    @SerializedName("craving")
    private String craving;

    public FoodDecideRequest(String mood, String budget, int companions, String craving) {
        this.mood = mood;
        this.budget = budget;
        this.companions = companions;
        this.craving = craving;
    }

    public String getMood() { return mood; }
    public String getBudget() { return budget; }
    public int getCompanions() { return companions; }
    public String getCraving() { return craving; }
}

