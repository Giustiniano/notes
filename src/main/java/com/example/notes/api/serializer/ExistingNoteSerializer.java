package com.example.notes.api.serializer;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class ExistingNoteSerializer extends TransientNoteSerializer {

    private UUID id;

    public ExistingNoteSerializer(UUID id, String title, String body, List<Tags> tags, LocalDate created) {
        super(title, body, tags, created);
        this.id = id;
    }

    public static ExistingNoteSerializer fromNoteModel(Note note){
        return new ExistingNoteSerializer(note.getId(), note.getTitle(), note.getBody(), note.getTags(),
                note.getCreated());
    }
}
