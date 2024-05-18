package com.example.notes.api;

import com.example.notes.model.Note;
import com.example.notes.model.Tags;
import com.example.notes.service.repository.NoteRepository;
import com.example.notes.utils.HateoasResponse;
import com.example.notes.utils.NoteResponse;
import com.example.notes.utils.NoteRequest;
import com.example.notes.utils.factories.NoteFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
                .map(c -> LocalDate.parse((String) c, formatter)).orElse(null);

        Page<Note> notes = noteRepository.findAll(Pageable.unpaged());
        assert notes.getTotalElements() == 1L;
        Note actualNote = notes.getContent().get(0);
        assert actualNote.getId().equals(UUID.fromString((String) responseBody.get("id")));
        assert actualNote.getTitle().equals(responseBody.get("title"));
        assert actualNote.getBody().equals(responseBody.get("body"));
        assert actualNote.getCreated() == null || actualNote.getCreated().equals(createdDateInResponse);
        assert actualNote.getTags() == null || actualNote.getTags().stream().map(Enum::toString).toList()
                .equals(responseBody.get("tags"));

    }

    @ParameterizedTest
    @MethodSource
    public void testSaveNewNoteWrongData(Map<String, Object> noteBody) {
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
    public void testGetFilterNotesByTag(){
        Map<String, Note> testData = createFilteringTestData();

        String url = createUrlWithPort() + "?tags=BUSINESS,IMPORTANT";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HateoasResponse> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                HateoasResponse.class);
        assert response.getStatusCode().value() == 200;
        HateoasResponse responseBody = response.getBody();
        List<HateoasResponse.Note> notes = responseBody.get_embedded().getNotes();
        assert notes.size() == 2;

        List<HateoasResponse.Note> onlyBusinessTaggedNotes = notes.stream().filter(n -> UUID.fromString(n.getId())
                .equals(testData.get("business").getId())).toList();
        assert onlyBusinessTaggedNotes.size() == 1;
        assert noteMatch(testData.get("business"), onlyBusinessTaggedNotes.get(0));

        List<HateoasResponse.Note> doubleTaggedNotes = notes.stream().filter(n -> UUID.fromString(n.getId())
                .equals(testData.get("businessImportant").getId())).toList();
        assert doubleTaggedNotes.size() == 1;
        assert noteMatch(testData.get("businessImportant"), doubleTaggedNotes.get(0));

    }

    @Test
    public void testGetUnfilteredNotes() {
        Map<String, Note> testData = createFilteringTestData();

        String url = createUrlWithPort();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HateoasResponse> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                HateoasResponse.class);
        assert response.getStatusCode().value() == 200;
        HateoasResponse responseBody = response.getBody();
        List<HateoasResponse.Note> notes = responseBody.get_embedded().getNotes();
        assert notes.size() == testData.size();
        // 20 is the default page size provided by spring boot if no paging is provided
        assert responseBody.getPage().equals(new HateoasResponse.Page(20, testData.size(), 1, 0));


        List<HateoasResponse.Note> onlyBusinessTaggedNotes = notes.stream().filter(n -> UUID.fromString(n.getId())
                .equals(testData.get("business").getId())).toList();
        assert onlyBusinessTaggedNotes.size() == 1;
        assert noteMatch(testData.get("business"), onlyBusinessTaggedNotes.get(0));

        List<HateoasResponse.Note> doubleTaggedNotes = notes.stream().filter(n -> UUID.fromString(n.getId())
                .equals(testData.get("businessImportant").getId())).toList();
        assert doubleTaggedNotes.size() == 1;
        assert noteMatch(testData.get("businessImportant"), doubleTaggedNotes.get(0));

        List<HateoasResponse.Note> personalNotes = notes.stream().filter(n -> UUID.fromString(n.getId())
                .equals(testData.get("personal").getId())).toList();
        assert personalNotes.size() == 1;
        assert noteMatch(testData.get("personal"), personalNotes.get(0));

    }
    @Test
    public void testGetNotesPagingAndSorting(){
        Map<String, Note> testData = createFilteringTestData();
        String url = createUrlWithPort() + "?page=0&size=2";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HateoasResponse> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                HateoasResponse.class);
        assert response.getStatusCode().value() == 200;
        HateoasResponse responseBody = response.getBody();
        List<HateoasResponse.Note> notes = responseBody.get_embedded().getNotes();
        assert notes.size() == 2;
        // checking notes are sorted by created date desc
        assert noteMatch(testData.get("businessImportant"), notes.get(0));
        assert noteMatch(testData.get("business"), notes.get(1));

        assert responseBody.get_links().getSelf().getHref().equals(url);
        assert responseBody.get_links().getFirst().getHref().equals(url);
        // check that there are two pages in total
        assert responseBody.get_links().getLast().getHref().endsWith("?page=1&size=2");
        assert responseBody.getPage().equals(new HateoasResponse.Page(2, testData.size(), 2, 0));

    }

    @Test
    public void testGetNotesUnpaginated(){
        Map<String, Note> testData = createFilteringTestData();
        String url = createUrlWithPort() + "?page=0&size=10";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HateoasResponse> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                HateoasResponse.class);
        assert response.getStatusCode().value() == 200;
        HateoasResponse responseBody = response.getBody();
        List<HateoasResponse.Note> notes = responseBody.get_embedded().getNotes();
        assert notes.size() == 3;
        // checking notes are sorted by created date desc
        assert noteMatch(testData.get("businessImportant"), notes.get(0));
        assert noteMatch(testData.get("business"), notes.get(1));
        assert noteMatch(testData.get("personal"), notes.get(2));

        assert responseBody.get_links().getFirst() == null;
        assert responseBody.get_links().getLast() == null;
        assert responseBody.get_links().getPrev() == null;
        assert responseBody.get_links().getNext() == null;
        assert responseBody.get_links().getSelf().getHref().equals(url);
        assert responseBody.getPage().equals(new HateoasResponse.Page(10, testData.size(), 1, 0));




    }
    @Test
    public void testGetNotesPageOutOfRange(){
        Map<String, Note> testData = createFilteringTestData();
        String url = createUrlWithPort() + "?page=1&size=3";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HateoasResponse> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                HateoasResponse.class);
        assert response.getStatusCode().value() == 200;
        HateoasResponse responseBody = response.getBody();
        assert responseBody.get_embedded() == null;
        assert responseBody.getPage().equals(new HateoasResponse.Page(3, testData.size(), 1, 1));

    }

    // GET note body

    @Test
    public void testGetNoteBody(){
        Note testNote = noteRepository.save(defaultNote());
        String url = createUrlWithPort() + "/" + testNote.getId() + "/body";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HashMap<String, String>> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<HashMap<String, String>>() {});
        assert response.getStatusCode().value() == 200;
        HashMap<String, String> responseBody = response.getBody();
        assert responseBody.get("body").equals(testNote.getBody());
    }

    @Test
    public void testGetNoteBodyNotExists(){
        String url = createUrlWithPort() + "/" + UUID.randomUUID() + "/body";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.GET, entity, String.class);
        // THEN
        assert response.getStatusCode().value() == 404;

    }

    @Test
    public void testGetNoteBodyInvalidUUID(){
        String url = createUrlWithPort() + "/" + "I am a UUID!" + "/body";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.GET, entity, String.class);
        // THEN
        assert response.getStatusCode().value() == 400;
        assert response.getBody().equals(
                "{\"status\":\"BAD_REQUEST\",\"title\":\"the note id is not a valid UUIDv4\",\"detailMessage\":null}");


    }

    // Update note
    @ParameterizedTest
    @MethodSource
    public void testUpdateNote(Map<String, Object> noteData) {

        Note existingNote = noteRepository.save(defaultNote());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        // WHEN
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noteData, headers);
        String url = createUrlWithPort() + "/" + existingNote.getId();
        ResponseEntity<NoteResponse> response = webTestClient.exchange(url,
                HttpMethod.PUT, entity, NoteResponse.class);

        // THEN
        Assertions.assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        NoteResponse responseBody = response.getBody();
        // make sure the note ID did not change
        existingNote = noteRepository.findById(existingNote.getId()).get();
        assert responseBody.getId().equals(existingNote.getId());
        assert responseBody.getTitle().equals(noteData.get("title"));
        assert responseBody.getBody().equals(noteData.get("body"));
        assert responseBody.getCreated() == null || responseBody.getCreated().equals(noteData.get("created"));
        assert responseBody.getTags() == null || Arrays.equals(Arrays.stream(responseBody.getTags()).map(t -> t).toArray(),
                Arrays.stream((Object[]) noteData.get("tags")).map(t -> t).toArray());

        assert existingNote.getTitle() == null || existingNote.getTitle().equals(responseBody.getTitle());
        assert existingNote.getBody() == null || existingNote.getBody().equals(responseBody.getBody());
        assert existingNote.getTags() == null  ||
                Arrays.equals(existingNote.getTags().stream().map(Tags::name).toArray(), responseBody.getTags());
        assert existingNote.getCreated() == null ||
                existingNote.getCreated().equals(LocalDate.parse(responseBody.getCreated(), formatter));

    }

    @ParameterizedTest
    @MethodSource
    public void testUpdateNoteWrongData(Map<String, Object> noteBody) {
        Note existingNote = noteRepository.save(defaultNote());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noteBody, headers);
        String url = createUrlWithPort() + "/" + existingNote.getId();
        ResponseEntity<HashMap<String, String>> response = webTestClient.exchange(url,
                HttpMethod.PUT, entity,
                new ParameterizedTypeReference<HashMap<String, String>>() {
                });

        // THEN
        Assertions.assertEquals(HttpStatusCode.valueOf(400), response.getStatusCode());
        HashMap<String, String> responseBody = response.getBody();
        assert responseBody.get("status").equals(HttpStatus.BAD_REQUEST.name());
        assert responseBody.get("title").equals("Note is not valid");
        existingNote = noteRepository.findById(existingNote.getId()).get();
        assert existingNote.getTitle().equals(defaultNote().getTitle());
        assert existingNote.getBody().equals(defaultNote().getBody());
        assert existingNote.getCreated().equals(LocalDate.parse("16/05/2024", formatter));
        assert existingNote.getTags().equals(List.of(Tags.BUSINESS));

    }

    @Test
    public void testUpdateNoteInvalidUUID(){
        String url = createUrlWithPort() + "/" + "I am a UUID!";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<NoteRequest> entity = new HttpEntity<>(new NoteRequest(
                "", "", null, null), headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.PUT, entity, String.class);
        // THEN
        assert response.getStatusCode().value() == 400;
        assert response.getBody().equals(
                "{\"status\":\"BAD_REQUEST\",\"title\":\"the note id is not a valid UUIDv4\",\"detailMessage\":null}");

    }
    @Test
    public void testUpdateNoteNoteNotExists(){
        String url = createUrlWithPort() + "/" + UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<NoteRequest> entity = new HttpEntity<>(new NoteRequest(
                "", "", null, null), headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.PUT, entity, String.class);
        // THEN
        assert response.getStatusCode().value() == 404;
    }

    @Test
    public void testDeleteNote(){
        Note noteToDelete = noteRepository.save(defaultNote());
        String url = createUrlWithPort() + "/" + noteToDelete.getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HashMap<String, String>> response = webTestClient.exchange(url, HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<HashMap<String, String>>() {});
        assert response.getStatusCode().value() == 204;
        assert noteRepository.findById(noteToDelete.getId()).isEmpty();
    }
    @Test
    public void testDeleteNoteInvalidUUID(){
        String url = createUrlWithPort() + "/" + "a very legal UUID";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.DELETE, entity,
                String.class);
        assert response.getStatusCode().value() == 400;
        assert response.getBody().equals(
                "{\"status\":\"BAD_REQUEST\",\"title\":\"the note id is not a valid UUIDv4\",\"detailMessage\":null}");
    }

    @Test
    public void testDeleteNoteNotExists(){
        String url = createUrlWithPort() + "/" + UUID.randomUUID();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.DELETE, entity,
                String.class);
        assert response.getStatusCode().value() == 404;
    }

    @Test
    public void testGetNoteStatistics(){
        Note note = noteRepository.save(defaultNote());
        String url = createUrlWithPort() + "/" + note.getId() + "/statistics";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HashMap<String, HashMap<String, Integer>>> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HashMap<String, HashMap<String, Integer>>> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<HashMap<String, HashMap<String, Integer>>>() {});
        assert response.getStatusCode().value() == 200;
        HashMap<String, HashMap<String, Integer>> responseBody = response.getBody();
        assert responseBody.size() == 1;
        Map<String, Integer> wordCount = responseBody.get("wordCount");
        assert wordCount.size() == 2;
        assert wordCount.get("hello").equals(2);
        assert wordCount.get("world").equals(1);
    }

    @Test
    public void testGetNoteStatisticsInvalidUUID(){
        String url = createUrlWithPort() + "/" + "a very legal UUID" + "/statistics";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                String.class);
        assert response.getStatusCode().value() == 400;
        assert response.getBody().equals(
                "{\"status\":\"BAD_REQUEST\",\"title\":\"the note id is not a valid UUIDv4\",\"detailMessage\":null}");
    }

    @Test
    public void testGetNoteStatisticsNotExists(){
        String url = createUrlWithPort() + "/" + UUID.randomUUID() + "/statistics";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // WHEN
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = webTestClient.exchange(url, HttpMethod.GET, entity,
                String.class);
        assert response.getStatusCode().value() == 404;
    }

    private boolean noteMatch(Note expectedNote, HateoasResponse.Note actualNote) {
        return actualNote.getTitle().equals(expectedNote.getTitle()) &&
                actualNote.getCreated().equals(expectedNote.getCreated()
                .format(formatter)) &&
                actualNote.getBody() == null; // the note body is to be returned by a different endpoint
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

    private Map<String, Note> createFilteringTestData(){
        return Map.of("business", noteRepository.save(new NoteFactory().setId(UUID.fromString("818b6f5e-7db9-4834-99ab-ef5e6fd5b12d"))
                .setTitle("Tagged note").setBody("This is a tagged note").setTags(List.of(Tags.BUSINESS))
                .setCreatedDate(LocalDate.of(2024, 5, 15)).build()),
        "personal",noteRepository.save(new NoteFactory().setId(UUID.fromString("1254fc96-37b5-4f56-98a4-57bb3fab306b"))
                .setTitle("Differently Tagged note").setBody("This is a note with a different tag")
                .setTags(List.of(Tags.PERSONAL)).setCreatedDate(LocalDate.of(2024, 5, 14))
                .build()),
        "businessImportant",noteRepository.save(new NoteFactory().setId(UUID.fromString("53548820-188c-4065-8083-8722efed11b7"))
                .setTitle("note with multiple tags").setBody("This is a note with two tags")
                .setTags(List.of(Tags.BUSINESS, Tags.IMPORTANT))
                .setCreatedDate(LocalDate.of(2024, 5, 16)).build()));
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
    static Stream<Arguments> testUpdateNote() {
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

    static Stream<Arguments> testUpdateNoteWrongData() {
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

    private Note defaultNote(){
        LinkedHashMap<String, Integer> wordCount = new LinkedHashMap<>();
        wordCount.put("hello", 2);
        wordCount.put("world", 1);
        return new NoteFactory().setTags(List.of(Tags.BUSINESS)).setBody("Hello hello world!")
                .setTitle("Title").setCreatedDate(LocalDate.parse("16/05/2024", formatter))
                .setId(UUID.randomUUID()).setWordCount(wordCount).build();
    }


}
