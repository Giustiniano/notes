package com.example.notes.api.serializer;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record NewNoteSerializer(UUID id,
                                @NotNull(message = "Notes must have a title")
                                @NotBlank(message="The title cannot be blank") String title,
                                @NotNull(message = "Notes must have a body")
                                @NotBlank(message="The body cannot be blank") String body,
                                List<Tags> tags,
                                String created) {

    public Note toNoteModel(){
        return new Note(this.id, this.title, this.body, Optional.ofNullable(this.created).map(Instant::parse)
                .orElse(null), this.tags);
    }

    public static NewNoteSerializer fromNoteModel(Note note){
        return new NewNoteSerializer(note.getId(), note.getTitle(), note.getBody(), note.getTags(),
                note.getCreated().toString());
    }
}
