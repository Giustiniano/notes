package com.example.notes.service.repository;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends MongoRepository<Note, UUID> {
    Page<Note> findAll(Pageable pageable);
    void deleteAll();
    Page<Note> findTitleCreatedByTagsIn(Pageable pageable, List<Tags> tags);
}
