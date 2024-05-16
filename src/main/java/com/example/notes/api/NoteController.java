package com.example.notes.api;

import com.example.notes.api.exception.InvalidParameterException;
import com.example.notes.api.exception.ResourceNotFoundException;
import com.example.notes.api.serializer.ExistingNoteSerializer;
import com.example.notes.api.serializer.NewNoteSerializer;
import com.example.notes.api.serializer.NoteBodySerializer;
import com.example.notes.api.serializer.NoteListSerializer;
import com.example.notes.model.Tags;
import com.example.notes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
    public NewNoteSerializer post(@RequestBody @Valid NewNoteSerializer note){
        return noteService.saveNewNote(note);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedModel<NoteListSerializer> get(@RequestParam(name = "tags", required = false) List<Tags> tags, Pageable pageable){
        return noteService.getNotes(Optional.ofNullable(pageable).orElse(Pageable.unpaged()), Optional.ofNullable(tags)
                .orElse(Arrays.stream(Tags.values()).toList()));
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public NoteBodySerializer get(@PathVariable(name="id") String id) throws ResourceNotFoundException {
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

}
