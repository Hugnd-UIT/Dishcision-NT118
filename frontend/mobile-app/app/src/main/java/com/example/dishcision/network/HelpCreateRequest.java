package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HelpCreateRequest {
    @SerializedName("subject")
    private String subject;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("email")
    private String email;
    @SerializedName("attachments")
    private List<AttachmentInput> attachments;

    public HelpCreateRequest(String subject, String title, String description, String email) {
        this(subject, title, description, email, null);
    }

    public HelpCreateRequest(String subject, String title, String description, String email, List<AttachmentInput> attachments) {
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.email = email;
        this.attachments = attachments;
    }

    public String getSubject() { return subject; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getEmail() { return email; }
    public List<AttachmentInput> getAttachments() { return attachments; }

    public static class AttachmentInput {
        @SerializedName("name")
        public String name;
        @SerializedName("subtitle")
        public String subtitle;
        @SerializedName("url")
        public String url;

        public AttachmentInput(String name, String subtitle, String url) {
            this.name = name;
            this.subtitle = subtitle;
            this.url = url;
        }
    }
}