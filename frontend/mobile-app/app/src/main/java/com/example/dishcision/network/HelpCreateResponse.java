package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class HelpCreateResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private HelpCreateData data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public HelpCreateData getData() { return data; }
    public String getMessage() { return message; }

    public static class HelpCreateData {
        @SerializedName("ticketId")
        private String ticketId;

        public String getTicketId() { return ticketId; }
    }
}