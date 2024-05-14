package com.example.notes.service;

import com.example.notes.api.serializer.NewNoteSerializer;
import com.example.notes.model.Note;
import com.example.notes.service.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.UUID;

@Component
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public NewNoteSerializer saveNewNote(NewNoteSerializer note) {
        Note entity = note.toNoteModel();
        entity.setId(UUID.randomUUID());
        entity.setCreated(Instant.now().with(ChronoField.NANO_OF_SECOND, 0));
        Note saved = noteRepository.save(entity);
        return NewNoteSerializer.fromNoteModel(saved);
    }
}
