package com.example.notes.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@Document("notes")
public class Note {
    private UUID id;
    private String title;
    private String body;
    private LocalDate created;
    private List<Tags> tags;
}
