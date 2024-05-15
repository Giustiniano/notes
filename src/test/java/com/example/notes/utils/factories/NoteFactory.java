package com.example.notes.utils.factories;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
public class NoteFactory {
    private UUID id;
    private String title;
    private String body;
    private LocalDate createdDate;
    private List<Tags> tags;

    public Note build() {
        return Note.builder().id(id).title(title).body(body).created(createdDate).tags(tags).build();
    }

}
