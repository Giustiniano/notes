package com.example.notes.api;

import com.example.notes.model.Note;
import com.example.notes.service.repository.NoteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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


    @Test
    public void testSaveNewNote() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // GIVEN
        Map<String, Object> body = Map.of("title", "the title", "body", "the body", "tags",
                new String[]{"PERSONAL"});
        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        String url = createUrlWithPort();
        ResponseEntity<HashMap<String, Object>> response = webTestClient.exchange(url,
                HttpMethod.POST, entity,
                new ParameterizedTypeReference<HashMap<String, Object>>() {});
        Assertions.assertEquals(HttpStatusCode.valueOf(201), response.getStatusCode());
        HashMap<String, Object> responseBody = response.getBody();


        // THEN
        UUID.fromString((String) responseBody.get("id")); // make sure the BE set a valid UUID
        List.of("title", "body").forEach(k -> {
            assert responseBody.get(k).equals(body.get(k)): String.
                    format("request value for '%s' differs from the response value!", k);
        });
        ZonedDateTime.parse((String) responseBody.get("created")); // make sure BE set a valid creation date
        Page<Note> notes = noteRepository.findAll(Pageable.unpaged());
        assert notes.getTotalElements() == 1L;
        Note actualNote = notes.getContent().get(0);
        assert actualNote.getId().equals(UUID.fromString((String) responseBody.get("id")));
        assert actualNote.getTitle().equals(responseBody.get("title"));
        assert actualNote.getBody().equals(responseBody.get("body"));
        assert actualNote.getCreated().equals(Instant.parse((String) responseBody.get("created")));
        assert actualNote.getTags().stream().map(Enum::toString).toList().equals(responseBody.get("tags"));
    }

    private String createUrlWithPort(){
        return String.format("http://localhost:%d%s", port, noteApi);
    }

}
