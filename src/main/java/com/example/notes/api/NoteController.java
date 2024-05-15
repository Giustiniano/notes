package com.example.notes.api;

import com.example.notes.api.serializer.ExistingNoteSerializer;
import com.example.notes.api.serializer.NewNoteSerializer;
import com.example.notes.api.serializer.NoteListSerializer;
import com.example.notes.model.Tags;
import com.example.notes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NewNoteSerializer post(@RequestBody @Valid NewNoteSerializer note){
        return noteService.saveNewNote(note);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NoteListSerializer> get(@RequestParam(name = "tags", required = false) List<Tags> tags, Pageable pageable){
        return noteService.getNotes(Optional.ofNullable(pageable).orElse(Pageable.unpaged()), Optional.ofNullable(tags)
                .orElse(List.of()));
    }
}
