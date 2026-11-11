package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class AccountModel {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("name")
    private String name;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("initials")
    private String initials;

    @SerializedName("status")
    private String status;

    public AccountModel(int id, String name, String initials, String status) {
        this.id = id;
        this.name = name;
        this.initials = initials;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getInitials() {
        return initials;
    }

    public String getStatus() {
        return status;
    }
}
