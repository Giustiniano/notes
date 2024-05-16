package com.example.notes.api.serializer;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
@Relation(collectionRelation = "notes")
public class NoteListSerializer extends RepresentationModel<NoteListSerializer> {
    @JsonProperty
    private UUID id;
    @JsonProperty
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate created;

    public static NoteListSerializer fromNoteModel(Note note){
        return new NoteListSerializer(note.getId(), note.getTitle(), note.getCreated());
    }

}
