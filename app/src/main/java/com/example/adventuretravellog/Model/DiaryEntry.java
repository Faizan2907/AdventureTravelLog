package com.example.adventuretravellog.Model;

public class DiaryEntry {
    private String id;
    private String mediaType;
    private String mediaUrl;
    private String text;

    public DiaryEntry() {
        // Default constructor required for Firebase
    }

    public DiaryEntry(String id, String mediaType, String mediaUrl, String text) {
        this.id = id;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
