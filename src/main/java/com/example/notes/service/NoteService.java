package com.example.notes.service;

import com.example.notes.api.exception.ResourceNotFoundException;
import com.example.notes.api.serializer.*;
import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import com.example.notes.service.repository.NoteRepository;
import com.example.notes.stats.WordCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private PagedResourcesAssembler<Note> pagedResourcesAssembler;
    @Autowired
    private NoteSerializerAssembler noteSerializerAssembler;

    public ExistingNoteSerializer saveNewNote(TransientNoteSerializer note) {
        Note entity = note.toNoteModel();
        entity.setId(UUID.randomUUID());
        entity.setWordCount(WordCounter.getWordCount(entity.getBody()));
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
    public ExistingNoteSerializer updateNote(UUID id, TransientNoteSerializer newNoteData){
        return noteRepository.findById(id).map(n -> {
            n.setTitle(newNoteData.getTitle());
            n.setBody(newNoteData.getBody());
            n.setTags(newNoteData.getTags());
            n.setCreated(newNoteData.getCreated());
            n.setWordCount(WordCounter.getWordCount(newNoteData.getBody()));
            return ExistingNoteSerializer.fromNoteModel(noteRepository.save(n));
        }).orElse(null);

    }

    public StatsSerializer getStatistics(UUID id){
        Note note = noteRepository.findWordCountById(id);
        return note == null ? null : new StatsSerializer(note.getWordCount());
    }

    public void deleteNote(UUID id) throws ResourceNotFoundException {
        Note note = noteRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        noteRepository.delete(note);
    }
}
