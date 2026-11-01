package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class SessionModel {

    @SerializedName("id")
    private String id;

    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("isCurrent")
    private boolean isCurrent;

    @SerializedName("statusText")
    private String statusText;

    @SerializedName("location")
    private String location;

    public SessionModel(String id, String deviceName, String subtitle, boolean isCurrent, String statusText, String location) {
        this.id = id;
        this.deviceName = deviceName;
        this.subtitle = subtitle;
        this.isCurrent = isCurrent;
        this.statusText = statusText;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getLocation() {
        return location;
    }
}
