package dev.stephen.reviews_tui_app.gateway.rest;

import dev.stephen.reviews_tui_app.colleagues.repository.ColleagueRepository;
import dev.stephen.reviews_tui_app.notes.NoteCategory;
import dev.stephen.reviews_tui_app.notes.NoteTag;
import dev.stephen.reviews_tui_app.notes.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ColleagueRepository colleagueRepository;

    @Autowired
    private NoteRepository noteRepository;

    private final JsonMapper jsonMapper = JsonMapper.shared();

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
        colleagueRepository.deleteAll();
    }

    private Long createColleague(String name) throws Exception {
        String body = mockMvc.perform(post("/api/v1/colleagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").longValue();
    }

    private String noteBody(Long colleagueId, String content, NoteCategory category, NoteTag tag) {
        return String.format(
                "{\"colleagueId\": %d, \"content\": \"%s\", \"category\": \"%s\", \"tag\": \"%s\"}",
                colleagueId, content, category.name(), tag.name());
    }

    // POST /api/v1/colleagues

    @Test
    void createColleague_returns201_withIdAndName() throws Exception {
        mockMvc.perform(post("/api/v1/colleagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Alice Smith\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    void createColleague_persistsColleagueToDatabase() throws Exception {
        mockMvc.perform(post("/api/v1/colleagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Alice Smith\"}"))
                .andExpect(status().isCreated());

        assertThat(colleagueRepository.count()).isEqualTo(1);
        assertThat(colleagueRepository.findAll().get(0).getName()).isEqualTo("Alice Smith");
    }

    // GET /api/v1/colleagues

    @Test
    void listColleagues_whenEmpty_returns200WithEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/colleagues"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listColleagues_returnsAllCreatedColleagues() throws Exception {
        createColleague("Alice Smith");
        createColleague("Bob Jones");

        mockMvc.perform(get("/api/v1/colleagues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name").value(containsInAnyOrder("Alice Smith", "Bob Jones")));
    }

    // GET /api/v1/colleagues/search?q={query}

    @Test
    void searchColleagues_substringMatch_returnsMatch() throws Exception {
        createColleague("Alice Smith");

        mockMvc.perform(get("/api/v1/colleagues/search").param("q", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice Smith"));
    }

    @Test
    void searchColleagues_fuzzyMatch_returnsMatch() throws Exception {
        createColleague("Alice Smith");

        mockMvc.perform(get("/api/v1/colleagues/search").param("q", "Alise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void searchColleagues_noMatch_returnsEmptyArray() throws Exception {
        createColleague("Alice Smith");

        mockMvc.perform(get("/api/v1/colleagues/search").param("q", "zzz"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // POST /api/v1/notes

    @Test
    void createNote_returns201_withContentAndColleagueInfo() throws Exception {
        Long colleagueId = createColleague("Alice Smith");

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(colleagueId, "Great technical work", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great technical work"))
                .andExpect(jsonPath("$.category").value("TECHNICAL_ABILITY"))
                .andExpect(jsonPath("$.colleague.name").value("Alice Smith"));
    }

    @Test
    void createNote_persistsNoteToDatabase() throws Exception {
        Long colleagueId = createColleague("Alice Smith");

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(colleagueId, "Good work", NoteCategory.GENERAL, NoteTag.NONE)))
                .andExpect(status().isCreated());

        assertThat(noteRepository.count()).isEqualTo(1);
    }

    @Test
    void createNote_unknownColleague_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(99999L, "Some note", NoteCategory.GENERAL, NoteTag.NONE)))
                .andExpect(status().isNotFound());
    }

    // GET /api/v1/notes/summary?colleague={name}

    @Test
    void getSummaryForColleague_returnsCorrectCounts() throws Exception {
        Long colleagueId = createColleague("Alice Smith");
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(colleagueId, "Highlight note", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(colleagueId, "Improvement note", NoteCategory.TECHNICAL_ABILITY, NoteTag.IMPROVEMENT)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/notes/summary").param("colleague", "Alice Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNotes").value(2))
                .andExpect(jsonPath("$.highlights").value(1))
                .andExpect(jsonPath("$.improvements").value(1))
                .andExpect(jsonPath("$.colleagueName").value("Alice Smith"));
    }

    @Test
    void getSummaryForColleague_notesByCategoryPresent() throws Exception {
        Long colleagueId = createColleague("Alice Smith");
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(colleagueId, "Tech note", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(colleagueId, "Improvement note", NoteCategory.GENERAL, NoteTag.IMPROVEMENT)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/notes/summary").param("colleague", "Alice Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notesByCategory.TECHNICAL_ABILITY").exists())
                .andExpect(jsonPath("$.notesByCategory.TECHNICAL_ABILITY.length()").value(1));
    }

    // GET /api/v1/notes/summary?from={date}&to={date}

    @Test
    void getSummaryForAll_groupsNotesByColleague() throws Exception {
        Long aliceId = createColleague("Alice Smith");
        Long bobId = createColleague("Bob Jones");
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(aliceId, "Alice note", NoteCategory.GENERAL, NoteTag.NONE)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noteBody(bobId, "Bob note", NoteCategory.GENERAL, NoteTag.NONE)))
                .andExpect(status().isCreated());

        String today = String.valueOf(LocalDate.now());
        mockMvc.perform(get("/api/v1/notes/summary")
                        .param("from", today)
                        .param("to", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // GET /api/v1/notes/summary (no params)

    @Test
    void getSummary_noParams_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/notes/summary"))
                .andExpect(status().isBadRequest());
    }
}
