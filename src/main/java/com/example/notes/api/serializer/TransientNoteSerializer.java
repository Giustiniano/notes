package com.example.notes.api.serializer;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TransientNoteSerializer {
    @NotNull(message = "Notes must have a title")
    protected String title;
    @NotNull(message = "Notes must have a body")
    protected String body;
    protected List<Tags> tags;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    protected LocalDate created;


    @JsonCreator
    public TransientNoteSerializer(String title, String body, List<Tags> tags, LocalDate created){
        this(title, body);
        this.created = created;
        this.tags = tags;
    }
    private TransientNoteSerializer(String title, String body){
        this.title = title;
        this.body = body;
    }

    public Note toNoteModel(){
        return new Note(null, this.title, this.body, this.created, this.tags);
    }
}
