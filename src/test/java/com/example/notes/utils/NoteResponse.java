package com.example.notes.utils;

import java.util.UUID;

public class NoteResponse {
    private String title, body, created;
    private String[] tags;
    private UUID id;

    public String getTitle() {
        return title;
    }

    public NoteResponse setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public NoteResponse setBody(String body) {
        this.body = body;
        return this;
    }

    public String getCreated() {
        return created;
    }

    public NoteResponse setCreated(String created) {
        this.created = created;
        return this;
    }

    public String[] getTags() {
        return tags;
    }

    public NoteResponse setTags(String[] tags) {
        this.tags = tags;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public NoteResponse setId(UUID id) {
        this.id = id;
        return this;
    }
}
