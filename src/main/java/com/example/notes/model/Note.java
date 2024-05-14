package com.example.notes.model;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Note {
    private UUID id;
    private String title;
    private String body;
    private Instant created;
    private List<Tags> tags;
}
