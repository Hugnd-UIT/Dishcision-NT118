package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FoodDecideResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private AIResultData data;

    public boolean isSuccess() { return success; }
    public AIResultData getData() { return data; }

    public static class AIResultData {
        @SerializedName("modelUsed")
        public String modelUsed;

        @SerializedName("suggestions")
        public List<Suggestion> suggestions;
    }

    public static class Suggestion {
        @SerializedName("rank")
        public int rank;

        @SerializedName("dishName")
        public String dishName;

        @SerializedName("reason")
        public String reason;

        @SerializedName("estimatedPrice")
        public String estimatedPrice;
    }
}

