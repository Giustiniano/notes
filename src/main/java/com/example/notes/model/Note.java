package com.example.notes.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
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
    private Map<String, Integer> wordCount;

    public Note(UUID id, String title, String body, LocalDate created, List<Tags> tags){
        this.id = id;
        this.title = title;
        this.body = body;
        this.created = created;
        this.tags = tags;
    }
}
