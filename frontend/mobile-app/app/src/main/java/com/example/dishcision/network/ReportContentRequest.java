package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class ReportContentRequest {
    @SerializedName("postId")
    private Integer postId;

    @SerializedName("reportedUserId")
    private Integer reportedUserId;

    @SerializedName("reason")
    private String reason;

    @SerializedName("description")
    private String description;

    public ReportContentRequest(Integer postId, Integer reportedUserId, String reason, String description) {
        this.postId = postId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.description = description;
    }

    public Integer getPostId() { return postId; }
    public Integer getReportedUserId() { return reportedUserId; }
    public String getReason() { return reason; }
    public String getDescription() { return description; }
}