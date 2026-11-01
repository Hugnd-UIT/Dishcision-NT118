package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class ReportContentResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private ReportContentData data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public ReportContentData getData() { return data; }
    public String getMessage() { return message; }

    public static class ReportContentData {
        @SerializedName("reportId")
        private int reportId;

        public int getReportId() { return reportId; }
    }
}