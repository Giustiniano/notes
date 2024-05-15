package com.example.notes.api.serializer;

import com.example.notes.model.Note;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
public class NoteListSerializer {
    @JsonProperty
    private UUID id;
    @JsonProperty
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    protected LocalDate created;

    public static NoteListSerializer fromNoteModel(Note note){
        return new NoteListSerializer(note.getId(), note.getTitle(), note.getCreated());
    }

}
