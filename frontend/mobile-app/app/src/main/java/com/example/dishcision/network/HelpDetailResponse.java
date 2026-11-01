package com.example.dishcision.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HelpDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private HelpDetailData data;

    public boolean isSuccess() { return success; }
    public HelpDetailData getData() { return data; }

    public static class HelpDetailData {
        @SerializedName("id")
        private String id;
        @SerializedName("title")
        private String title;
        @SerializedName("date")
        private String date;
        @SerializedName("category")
        private String category;
        @SerializedName("status")
        private String status;
        @SerializedName("submittedContent")
        private String submittedContent;
        @SerializedName("submissionMethod")
        private String submissionMethod;
        @SerializedName("contactEmail")
        private String contactEmail;
        @SerializedName("attachments")
        private List<Attachment> attachments;
        @SerializedName("timeline")
        private List<TimelineItem> timeline;

        public String getTitle() { return title; }
        public String getDate() { return date; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getSubmittedContent() { return submittedContent; }
        public String getSubmissionMethod() { return submissionMethod; }
        public String getContactEmail() { return contactEmail; }
        public List<Attachment> getAttachments() { return attachments; }
        public List<TimelineItem> getTimeline() { return timeline; }
    }

    public static class Attachment {
        @SerializedName("name")
        private String name;
        @SerializedName("subtitle")
        private String subtitle;

        public String getName() { return name; }
        public String getSubtitle() { return subtitle; }
    }

    public static class TimelineItem {
        @SerializedName("step")
        private String step;
        @SerializedName("date")
        private String date;
        @SerializedName("desc")
        private String desc;
        @SerializedName("isDone")
        private boolean isDone;

        public String getStep() { return step; }
        public String getDate() { return date; }
        public String getDesc() { return desc; }
        public boolean isDone() { return isDone; }
    }
}