package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CommentsListResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public List<CommentItem> data;

    public static class CommentItem {
        @SerializedName("commentId")
        public int commentId;

        @SerializedName("content")
        public String content;

        @SerializedName("createdAt")
        public String createdAt;

        @SerializedName("author")
        public FeedPostsResponse.Author author;

        @SerializedName("likesCount")
        public int likesCount;

        @SerializedName("replies")
        public List<CommentItem> replies;
    }
}
