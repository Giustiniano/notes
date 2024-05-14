package com.example.notes.api;

import com.example.notes.api.serializer.NewNoteSerializer;
import com.example.notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping(path = "/api/v1/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NewNoteSerializer post(@RequestBody NewNoteSerializer note){
        return noteService.saveNewNote(note);
    }
}
