package com.example.notes.utils;

public class NoteRequest {
    private String title, body, created;
    private String[] tags;

    public NoteRequest(String title, String body, String created, String[] tags) {
        this.title = title;
        this.body = body;
        this.created = created;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public NoteRequest setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public NoteRequest setBody(String body) {
        this.body = body;
        return this;
    }

    public String getCreated() {
        return created;
    }

    public NoteRequest setCreated(String created) {
        this.created = created;
        return this;
    }

    public String[] getTags() {
        return tags;
    }

    public NoteRequest setTags(String[] tags) {
        this.tags = tags;
        return this;
    }

}
