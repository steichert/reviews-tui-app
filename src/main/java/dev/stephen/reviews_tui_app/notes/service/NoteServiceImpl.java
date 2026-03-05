package dev.stephen.reviews_tui_app.notes.service;

import dev.stephen.reviews_tui_app.colleagues.dto.ColleagueDto;
import dev.stephen.reviews_tui_app.notes.ColleagueSummaryDto;
import dev.stephen.reviews_tui_app.notes.NoteService;
import dev.stephen.reviews_tui_app.notes.dto.ReviewNoteDto;
import dev.stephen.reviews_tui_app.notes.entity.ReviewNote;
import dev.stephen.reviews_tui_app.notes.model.NoteCategory;
import dev.stephen.reviews_tui_app.notes.model.NoteTag;
import dev.stephen.reviews_tui_app.notes.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public ReviewNoteDto createNote(ReviewNoteDto noteDto, ColleagueDto colleagueDto) {
        ReviewNote note = ReviewNote.builder()
                .content(noteDto.content())
                .category(noteDto.category())
                .tag(noteDto.tag())
                .colleagueId(colleagueDto.id())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ReviewNote saved = noteRepository.save(note);
        return new ReviewNoteDto(saved.getContent(), saved.getCategory(), saved.getTag(), colleagueDto, saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Override
    public ColleagueSummaryDto getSummaryForColleague(String colleagueName) {
        List<ReviewNote> notes = noteRepository.findByColleague_Name(colleagueName);
        return buildSummary(colleagueName, notes);
    }

    @Override
    public List<ColleagueSummaryDto> getSummaryForAll(LocalDate from, LocalDate to) {
        List<ReviewNote> notes = noteRepository.findByCreatedAtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX));
        Map<String, List<ReviewNote>> byColleague = notes.stream()
                .filter(n -> n.getColleague() != null)
                .collect(Collectors.groupingBy(n -> n.getColleague().getName()));
        return byColleague.entrySet().stream()
                .map(e -> buildSummary(e.getKey(), e.getValue()))
                .toList();
    }

    private ColleagueSummaryDto buildSummary(String colleagueName, List<ReviewNote> notes) {
        int highlights = (int) notes.stream().filter(n -> n.getTag() == NoteTag.HIGHLIGHT).count();
        int improvements = (int) notes.stream().filter(n -> n.getTag() == NoteTag.IMPROVEMENT).count();
        Map<NoteCategory, List<ReviewNoteDto>> notesByCategory = notes.stream()
                .collect(Collectors.groupingBy(
                        ReviewNote::getCategory,
                        Collectors.mapping(this::mapNoteToDto, Collectors.toList())));
        return new ColleagueSummaryDto(colleagueName, notes.size(), highlights, improvements, notesByCategory);
    }

    private ReviewNoteDto mapNoteToDto(ReviewNote note) {
        ColleagueDto colleagueDto = null;
        if (note.getColleague() != null) {
            colleagueDto = new ColleagueDto(
                    note.getColleague().getId(),
                    note.getColleague().getName(),
                    note.getColleague().getCreatedAt(),
                    note.getColleague().getUpdatedAt());
        }
        return new ReviewNoteDto(note.getContent(), note.getCategory(), note.getTag(), colleagueDto, note.getCreatedAt(), note.getUpdatedAt());
    }
}
