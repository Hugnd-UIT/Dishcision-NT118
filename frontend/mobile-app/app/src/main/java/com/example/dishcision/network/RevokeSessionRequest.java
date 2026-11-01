package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RevokeSessionRequest {

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("sessionIds")
    private List<String> sessionIds;

    public RevokeSessionRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    public RevokeSessionRequest(List<String> sessionIds) {
        this.sessionIds = sessionIds;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<String> getSessionIds() {
        return sessionIds;
    }
}
