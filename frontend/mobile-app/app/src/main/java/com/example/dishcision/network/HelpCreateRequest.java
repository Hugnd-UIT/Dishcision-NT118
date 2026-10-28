package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;

public class HelpCreateRequest {
    @SerializedName("subject")
    private String subject;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("email")
    private String email;

    public HelpCreateRequest(String subject, String title, String description, String email) {
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.email = email;
    }

    public String getSubject() { return subject; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getEmail() { return email; }
}