package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AccountsListResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<AccountModel> data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public List<AccountModel> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
