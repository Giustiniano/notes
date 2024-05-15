package com.example.notes.utils.factories;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NoteFactory {
    private UUID id;
    private String title;
    private String body;
    private LocalDate createdDate;
    private List<Tags> tags;


    public UUID getId() {
        return id;
    }

    public NoteFactory setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public NoteFactory setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public NoteFactory setBody(String body) {
        this.body = body;
        return this;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public NoteFactory setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public List<Tags> getTags() {
        return tags;
    }

    public NoteFactory setTags(List<Tags> tags) {
        this.tags = tags;
        return this;
    }

    public Note build() {
        return Note.builder().id(id).title(title).body(body).created(createdDate).tags(tags).build();
    }

}
