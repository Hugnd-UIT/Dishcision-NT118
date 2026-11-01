package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class AccountActionRequest {

    @SerializedName("accountId")
    private int accountId;

    public AccountActionRequest(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
