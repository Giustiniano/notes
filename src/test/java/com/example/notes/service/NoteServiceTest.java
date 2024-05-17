package com.example.notes.service;

import com.example.notes.api.exception.ResourceNotFoundException;
import com.example.notes.api.serializer.NoteBodySerializer;
import com.example.notes.api.serializer.TransientNoteSerializer;
import com.example.notes.model.Note;
import com.example.notes.service.repository.NoteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {
    @Mock
    private NoteRepository noteRepository;
    @InjectMocks
    private NoteService noteService;

    @Test
    public void testAddIdAndWordCountOnSaveNewNote(){
        ArgumentCaptor<Note> newNoteArgument = ArgumentCaptor.forClass(Note.class);
        when(noteRepository.save(any())).thenReturn(new Note(UUID.randomUUID(), "a title", "a a body", null, null, null));
        TransientNoteSerializer newNote = new TransientNoteSerializer(
                "a title", "a a body", null, null);
        noteService.saveNewNote(newNote);
        verify(noteRepository).save(newNoteArgument.capture());
        Note savedNote = newNoteArgument.getValue();
        Assertions.assertNotNull(savedNote.getId());
        Assertions.assertNotNull(savedNote.getWordCount());
        assert savedNote.getWordCount().size() == 2;
        Iterator<Map.Entry<String, Integer>> wcIterator = savedNote.getWordCount().entrySet().iterator();
        Map.Entry<String, Integer> entry = wcIterator.next();
        Assertions.assertEquals("a", entry.getKey());
        Assertions.assertEquals(2, entry.getValue());
        entry =  wcIterator.next();
        Assertions.assertEquals("body", entry.getKey());
        Assertions.assertEquals(1, entry.getValue());
        Assertions.assertFalse(wcIterator.hasNext());
        Assertions.assertEquals(savedNote.getTitle(), newNote.getTitle());
        Assertions.assertEquals(savedNote.getBody(), newNote.getBody());
        Assertions.assertEquals(savedNote.getTags(), newNote.getTags());
        Assertions.assertEquals(savedNote.getCreated(), newNote.getCreated());
    }
    @Test
    public void testUpdateWordCountWhenUpdatingNote(){
        UUID noteUUID = UUID.randomUUID();
        ArgumentCaptor<Note> updatedNoteArgument = ArgumentCaptor.forClass(Note.class);
        when(noteRepository.findById(any())).thenReturn(Optional.of(
                new Note(noteUUID, "a title", "a a body", null, null,
                        Map.of("a", 2, "body", 1))));
        TransientNoteSerializer newNoteData = new TransientNoteSerializer("new title", "a body body",
                null, null);
        when(noteRepository.save(any())).thenReturn(new Note(noteUUID, "new title", "a body body", null, null, null));
        noteService.updateNote(noteUUID, newNoteData);
        verify(noteRepository).save(updatedNoteArgument.capture());
        Note updatedNote = updatedNoteArgument.getValue();
        Assertions.assertEquals(noteUUID, updatedNote.getId());
        Assertions.assertEquals(newNoteData.getTitle(), updatedNote.getTitle());
        Assertions.assertEquals(newNoteData.getBody(), updatedNote.getBody());
        Assertions.assertEquals(newNoteData.getTags(), updatedNote.getTags());
        Assertions.assertEquals(newNoteData.getCreated(), updatedNote.getCreated());
        assert updatedNote.getWordCount().size() == 2;
        Iterator<Map.Entry<String, Integer>> wcIterator = updatedNote.getWordCount().entrySet().iterator();
        Map.Entry<String, Integer> entry = wcIterator.next();
        Assertions.assertEquals("body", entry.getKey());
        Assertions.assertEquals(2, entry.getValue());
        entry =  wcIterator.next();
        Assertions.assertEquals("a", entry.getKey());
        Assertions.assertEquals(1, entry.getValue());
        Assertions.assertFalse(wcIterator.hasNext());
    }

    @Test
    public void testUpdateNoteReturnNullIfNotFound(){
        when(noteRepository.findById(any())).thenReturn(Optional.empty());
        Assertions.assertNull(noteService.updateNote(UUID.randomUUID(),
                new TransientNoteSerializer(null, null, null, null)));
    }

    @Test
    public void testThrowExceptionIfNoteToDeleteDoesNotExists(){
        when(noteRepository.findById(any())).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> noteService.deleteNote(UUID.randomUUID()));
    }
    @Test
    public void testGetNoteBody(){
        Note note = new Note(UUID.randomUUID(), "title", "the body", null, null, null);
        when(noteRepository.findBodyById(note.getId())).thenReturn(note);
        NoteBodySerializer result = noteService.getNoteBody(note.getId());
        assert result.body().equals(note.getBody());
    }

    @Test
    public void testGetStatistics(){
        Note note = new Note(UUID.randomUUID(), null, null, null, null, Map.of("body", 1));
        when(noteRepository.findWordCountById(note.getId())).thenReturn(note);
        noteService.getStatistics(note.getId()).getWordCount().equals(note.getWordCount());
    }

}
