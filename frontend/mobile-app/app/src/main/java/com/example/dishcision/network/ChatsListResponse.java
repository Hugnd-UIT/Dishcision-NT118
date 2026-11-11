package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatsListResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<ConversationItem> data;

    public static class ConversationItem {
        @SerializedName("recipient")
        public FeedPostsResponse.Author recipient;

        @SerializedName("lastMessage")
        public ChatMessagesResponse.MessageItem lastMessage;

        @SerializedName("unreadCount")
        public int unreadCount;
    }
}

