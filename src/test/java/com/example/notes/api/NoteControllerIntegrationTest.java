package com.example.notes.api;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import com.example.notes.service.repository.NoteRepository;
import com.example.notes.utils.factories.NoteFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class NoteControllerIntegrationTest {
    @Container
    @ServiceConnection
    static MongoDBContainer mongoDb = new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    @Autowired
    private TestRestTemplate webTestClient;
    @Autowired
    private NoteRepository noteRepository;
    @LocalServerPort
    private int port;
    private static final String noteApi = "/api/v1/note";

    @BeforeEach
    public void before() {
        noteRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource
    public void testSaveNewNote(Map<String, Object> noteBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        // WHEN
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noteBody, headers);
        String url = createUrlWithPort();
        ResponseEntity<HashMap<String, Object>> response = webTestClient.exchange(url,
                HttpMethod.POST, entity,
                new ParameterizedTypeReference<HashMap<String, Object>>() {
                });

        // THEN
        Assertions.assertEquals(HttpStatusCode.valueOf(201), response.getStatusCode());
        HashMap<String, Object> responseBody = response.getBody();
        UUID.fromString((String) responseBody.get("id")); // make sure the BE set a valid UUID
        List.of("title", "body").forEach(k -> {
            assert responseBody.get(k).equals(noteBody.get(k)) : String.
                    format("request value for '%s' differs from the response value!", k);
        });
        LocalDate createdDateInResponse = Optional.ofNullable(responseBody.getOrDefault("created", null))
                .map(c -> LocalDate.parse((String) c, DateTimeFormatter.ofPattern("dd/MM/yyyy"))).orElse(null);

        Page<Note> notes = noteRepository.findAll(Pageable.unpaged());
        assert notes.getTotalElements() == 1L;
        Note actualNote = notes.getContent().get(0);
        assert actualNote.getId().equals(UUID.fromString((String) responseBody.get("id")));
        assert actualNote.getTitle().equals(responseBody.get("title"));
        assert actualNote.getBody().equals(responseBody.get("body"));
        assert actualNote.getCreated() == null || actualNote.getCreated().equals(createdDateInResponse);
        assert actualNote.getTags() == null || actualNote.getTags().stream().map(Enum::toString).toList()
                .equals(responseBody.get("tags"));
        //assert
    }

    @ParameterizedTest
    @MethodSource
    public void testSaveNewNoteWrongData(Map<String, Object> noteBody) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noteBody, headers);
        String url = createUrlWithPort();
        ResponseEntity<HashMap<String, String>> response = webTestClient.exchange(url,
                HttpMethod.POST, entity,
                new ParameterizedTypeReference<HashMap<String, String>>() {
                });
        Assertions.assertEquals(HttpStatusCode.valueOf(400), response.getStatusCode());
        HashMap<String, String> responseBody = response.getBody();
        assert responseBody.get("status").equals(HttpStatus.BAD_REQUEST.name());
        assert responseBody.get("title").equals("Note is not valid");
        // THEN
        Page<Note> notes = noteRepository.findAll(Pageable.unpaged());
        assert notes.getTotalElements() == 0;
    }

    // GET endpoint
    @Test
    public void testFilterNotesByTag(){
        noteRepository.save(new NoteFactory().setId(UUID.fromString("818b6f5e-7db9-4834-99ab-ef5e6fd5b12d"))
                .setTitle("Tagged note").setBody("This is a tagged note").setTags(List.of(Tags.BUSINESS))
                .setCreatedDate(LocalDate.of(2024, 5, 15)).build());
        noteRepository.save(new NoteFactory().setId(UUID.fromString("1254fc96-37b5-4f56-98a4-57bb3fab306b"))
                .setTitle("Differently Tagged note").setBody("This is a note with a different tag")
                .setTags(List.of(Tags.PERSONAL)).setCreatedDate(LocalDate.of(2024, 5, 15))
                .build());
        noteRepository.save(new NoteFactory().setId(UUID.fromString("53548820-188c-4065-8083-8722efed11b7"))
                .setTitle("note with multiple tags").setBody("This is a note with two tags")
                .setTags(List.of(Tags.BUSINESS, Tags.IMPORTANT))
                .setCreatedDate(LocalDate.of(2024, 5, 15)).build());

        String url = createUrlWithPort() + "?tags=BUSINESS";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(null, headers);
        ResponseEntity<List<HashMap<String, String>>> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<HashMap<String, String>>>() {});
        assert response.getStatusCode().value() == 200;
        List<HashMap<String, String>> notes = response.getBody();
        assert notes.size() == 2;
        List<HashMap<String, String>> singleTaggedNotes = notes.stream().filter(n -> n.get("id").equals("818b6f5e-7db9-4834-99ab-ef5e6fd5b12d")).toList();
        assert singleTaggedNotes.size() == 1;
        HashMap<String, String> singleTaggedNote = singleTaggedNotes.get(0);
        assert singleTaggedNote.get("title").equals("Tagged note");
        assert singleTaggedNote.get("created").equals("15/05/2024");
        assert !singleTaggedNote.containsKey("body"); // the note body is to be returned by a different endpoint

    }

    private String createUrlWithPort() {
        return String.format("http://localhost:%d%s", port, noteApi);
    }

    private static Map<String, Object> buildNote(String title, String body, String[] tags, String created,
                                                 boolean includeNull) {
        Map<String, Object> note = new HashMap<>();
        if (title != null || includeNull) {
            note.put("title", title);
        }
        if (body != null || includeNull) {
            note.put("body", body);
        }
        if (tags != null || includeNull) {
            note.put("tags", tags);
        }
        if (created != null || includeNull) {
            note.put("created", created);
        }
        return note;

    }

    static Stream<Arguments> testSaveNewNote() {
        return Stream.of(
                Arguments.of(buildNote("the title", "the body", new String[]{"PERSONAL"}, "15/05/2024", false)),
                Arguments.of(buildNote("the title", "the body", new String[]{"PERSONAL", "IMPORTANT"}, "15/05/2024", false)),
                Arguments.of(buildNote("the title", "the body", null, null, false)),
                Arguments.of(buildNote("the title", "the body", null, null, true)),
                Arguments.of(buildNote("", "", null, null, false)),
                Arguments.of(buildNote("", "", null, null, true)));
    }

    static Stream<Arguments> testSaveNewNoteWrongData() {
        return Stream.of(
                Arguments.of(buildNote(null, null, null, null, true)),
                Arguments.of(buildNote(null, null, null, null, false)),
                Arguments.of(buildNote(null, "body", new String[]{"PERSONAL"}, "15/05/2024", true)),
                Arguments.of(buildNote(null, "body", new String[]{"PERSONAL"}, "15/05/2024", false)),
                Arguments.of(buildNote("title", null, new String[]{"BUSINESS"}, "15/05/2024", true)),
                Arguments.of(buildNote("title", null, new String[]{"BUSINESS"}, "15/05/2024", false)),
                Arguments.of(buildNote("title", "body", new String[]{"VERY_PERSONAL"}, "15/05/2024", false))
                );
    }


}
