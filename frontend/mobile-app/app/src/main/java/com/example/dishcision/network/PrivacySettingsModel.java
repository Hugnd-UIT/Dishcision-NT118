package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class PrivacySettingsModel {

    @SerializedName("newPosts")
    private String newPosts;

    @SerializedName("profileInfo")
    private String profileInfo;

    @SerializedName("followingList")
    private String followingList;

    @SerializedName("findEmail")
    private String findEmail;

    @SerializedName("friendRequests")
    private String friendRequests;

    @SerializedName("messages")
    private String messages;

    @SerializedName("comments")
    private String comments;

    public String getNewPosts() {
        return newPosts;
    }

    public void setNewPosts(String newPosts) {
        this.newPosts = newPosts;
    }

    public String getProfileInfo() {
        return profileInfo;
    }

    public void setProfileInfo(String profileInfo) {
        this.profileInfo = profileInfo;
    }

    public String getFollowingList() {
        return followingList;
    }

    public void setFollowingList(String followingList) {
        this.followingList = followingList;
    }

    public String getFindEmail() {
        return findEmail;
    }

    public void setFindEmail(String findEmail) {
        this.findEmail = findEmail;
    }

    public String getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(String friendRequests) {
        this.friendRequests = friendRequests;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
