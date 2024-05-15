package com.example.notes.service;

import com.example.notes.api.serializer.ExistingNoteSerializer;
import com.example.notes.api.serializer.NewNoteSerializer;
import com.example.notes.api.serializer.NoteListSerializer;
import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import com.example.notes.service.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;

@Component
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public ExistingNoteSerializer saveNewNote(NewNoteSerializer note) {
        Note entity = note.toNoteModel();
        entity.setId(UUID.randomUUID());
        Note saved = noteRepository.save(entity);
        return ExistingNoteSerializer.fromNoteModel(saved);
    }
    public List<NoteListSerializer> getNotes(Pageable pageable, List<Tags> tags){
        return noteRepository.findTitleCreatedByTagsIn(pageable, tags).getContent().stream()
                .map(NoteListSerializer::fromNoteModel).toList();
    }
}
