package com.example.notes.api.serializer;

import com.example.notes.model.Note;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class NoteSerializerAssembler implements RepresentationModelAssembler<Note, NoteListSerializer> {

    @Override
    public NoteListSerializer toModel(Note entity) {
        return new NoteListSerializer(entity.getId(), entity.getTitle(), entity.getCreated());
    }

    @Override
    public CollectionModel<NoteListSerializer> toCollectionModel(Iterable<? extends Note> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
