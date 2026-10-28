package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessionsResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<SessionModel> data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public List<SessionModel> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
