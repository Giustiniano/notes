package com.example.notes.api;

import com.example.notes.api.exception.InvalidParameterException;
import com.example.notes.api.exception.ResourceNotFoundException;
import com.example.notes.api.serializer.*;
import com.example.notes.model.Tags;
import com.example.notes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/api/v1/note")
public class NoteController {

    @Autowired
    private NoteService noteService;
    @Autowired
    private PagedResourcesAssembler<NoteListSerializer> pagedResourcesAssembler;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TransientNoteSerializer post(@RequestBody @Valid TransientNoteSerializer note){
        return noteService.saveNewNote(note);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<NoteListSerializer> get(@RequestParam(name = "tags", required = false) List<Tags> tags, Pageable pageable){
        return noteService.getNotes(Optional.ofNullable(pageable).orElse(Pageable.unpaged()), Optional.ofNullable(tags)
                .orElse(Arrays.stream(Tags.values()).toList()));
    }

    @GetMapping(value = "{id}/body", produces = MediaType.APPLICATION_JSON_VALUE)
    public NoteBodySerializer getStatistics(@PathVariable(name="id") String id) throws ResourceNotFoundException {
        UUID noteId;
        try{
            noteId = UUID.fromString(id);
        } catch (IllegalArgumentException ex){
            throw new InvalidParameterException("the note id is not a valid UUIDv4");
        }
        NoteBodySerializer noteBody = noteService.getNoteBody(noteId);
        if(noteBody == null){
            throw new ResourceNotFoundException();
        }
        return noteBody;
    }

    @GetMapping(value = "{id}/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public StatsSerializer get(@PathVariable(name="id") String id) throws ResourceNotFoundException {
        UUID noteId;
        try{
            noteId = UUID.fromString(id);
        } catch (IllegalArgumentException ex){
            throw new InvalidParameterException("the note id is not a valid UUIDv4");
        }
        StatsSerializer noteStatistics = noteService.getStatistics(noteId);
        if(noteStatistics == null){
            throw new ResourceNotFoundException();
        }
        return noteStatistics;
    }

    @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExistingNoteSerializer put(@PathVariable(name="id") String id,
                                      @RequestBody @Valid TransientNoteSerializer note)
            throws ResourceNotFoundException {
        UUID noteId;
        try{
            noteId = UUID.fromString(id);
        } catch (IllegalArgumentException ex){
            throw new InvalidParameterException("the note id is not a valid UUIDv4");
        }
        ExistingNoteSerializer noteBody = noteService.updateNote(noteId, note);
        if(noteBody == null){
            throw new ResourceNotFoundException();
        }
        return noteBody;
    }

    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity delete(@PathVariable(name="id") String id) {
        UUID noteId;
        try{
            noteId = UUID.fromString(id);
        } catch (IllegalArgumentException ex){
            throw new InvalidParameterException("the note id is not a valid UUIDv4");
        }
        noteService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }

}
