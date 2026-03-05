package dev.stephen.reviews_tui_app.gateway.rest;

import dev.stephen.reviews_tui_app.colleagues.ColleagueService;
import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;
import dev.stephen.reviews_tui_app.gateway.rest.dto.CreateColleagueRequest;
import dev.stephen.reviews_tui_app.gateway.rest.dto.CreateNoteRequest;
import dev.stephen.reviews_tui_app.notes.ColleagueSummaryDto;
import dev.stephen.reviews_tui_app.notes.NoteService;
import dev.stephen.reviews_tui_app.notes.ReviewNoteDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ColleagueService colleagueService;
    private final NoteService noteService;

    public ReviewController(ColleagueService colleagueService, NoteService noteService) {
        this.colleagueService = colleagueService;
        this.noteService = noteService;
    }

    @PostMapping(path = "/colleagues")
    public ResponseEntity<ColleagueDto> createColleague(@RequestBody CreateColleagueRequest request) {
        ColleagueDto created = colleagueService.createColleague(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(path = "/colleagues")
    public ResponseEntity<List<ColleagueDto>> listColleagues() {
        return ResponseEntity.ok(colleagueService.listColleagues());
    }

    @GetMapping(path = "/colleagues/search")
    public ResponseEntity<List<ColleagueDto>> searchColleagues(@RequestParam String q) {
        return ResponseEntity.ok(colleagueService.findByNameFuzzy(q));
    }

    @PostMapping(path = "/notes")
    public ResponseEntity<ReviewNoteDto> createNote(@RequestBody CreateNoteRequest request) {
        Optional<ColleagueDto> colleagueOpt = colleagueService.findById(request.colleagueId());
        if (colleagueOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ColleagueDto colleagueDto = colleagueOpt.get();
        ReviewNoteDto noteDto = new ReviewNoteDto(request.content(), request.category(), request.tag(), null, null, null);
        ReviewNoteDto created = noteService.createNote(noteDto, colleagueDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(path = "/notes/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(required = false) String colleague,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        if (colleague != null) {
            ColleagueSummaryDto summary = noteService.getSummaryForColleague(colleague);
            return ResponseEntity.ok(summary);
        } else if (from != null && to != null) {
            List<ColleagueSummaryDto> summaries = noteService.getSummaryForAll(from, to);
            return ResponseEntity.ok(summaries);
        } else {
            return ResponseEntity.badRequest().body("Provide either 'colleague' or both 'from' and 'to' parameters");
        }
    }
}
