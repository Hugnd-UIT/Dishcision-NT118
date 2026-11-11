package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatMessagesResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<MessageItem> data;

    public static class MessageItem {
        @SerializedName("messageId")
        public int messageId;

        @SerializedName("senderId")
        public int senderId;

        @SerializedName("recipientId")
        public int recipientId;

        @SerializedName("content")
        public String content;

        @SerializedName("mediaUrl")
        public String mediaUrl;

        @SerializedName("isRead")
        public boolean isRead;

        @SerializedName("createdAt")
        public String createdAt;

        @SerializedName("sender")
        public FeedPostsResponse.Author sender;
    }
}

