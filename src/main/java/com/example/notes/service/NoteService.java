package com.example.notes.service;

import com.example.notes.api.serializer.*;
import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import com.example.notes.service.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
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
    @Autowired
    private PagedResourcesAssembler<Note> pagedResourcesAssembler;
    @Autowired
    private NoteSerializerAssembler noteSerializerAssembler;

    public ExistingNoteSerializer saveNewNote(NewNoteSerializer note) {
        Note entity = note.toNoteModel();
        entity.setId(UUID.randomUUID());
        Note saved = noteRepository.save(entity);
        return ExistingNoteSerializer.fromNoteModel(saved);
    }
    public PagedModel<NoteListSerializer> getNotes(Pageable pageable, List<Tags> tags){
        Page<Note> entities = noteRepository.findTitleCreatedByTagsInOrderByCreatedDesc(pageable, tags);
        return pagedResourcesAssembler.toModel(entities, noteSerializerAssembler);
    }
    public NoteBodySerializer getNoteBody(UUID id){
        Note note = noteRepository.findBodyById(id);
        return note == null ? null : new NoteBodySerializer(note.getBody());
    }
}
