package dev.stephen.reviews_tui_app.notes.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;
import dev.stephen.reviews_tui_app.colleagues.ColleagueService;
import dev.stephen.reviews_tui_app.notes.ColleagueSummaryDto;
import dev.stephen.reviews_tui_app.notes.NoteCategory;
import dev.stephen.reviews_tui_app.notes.NoteService;
import dev.stephen.reviews_tui_app.notes.NoteTag;
import dev.stephen.reviews_tui_app.notes.ReviewNoteDto;
import dev.stephen.reviews_tui_app.notes.entity.ReviewNote;
import dev.stephen.reviews_tui_app.notes.repository.NoteRepository;

@Service
public class NoteServiceImpl implements NoteService {

  private final NoteRepository noteRepository;
  private final ColleagueService colleagueService;

  public NoteServiceImpl(NoteRepository noteRepository, ColleagueService colleagueService) {
    this.noteRepository = noteRepository;
    this.colleagueService = colleagueService;
  }

  @Override
  public ReviewNoteDto createNote(ReviewNoteDto noteDto, ColleagueDto colleagueDto) {
    ReviewNote note =
        ReviewNote.builder()
            .content(noteDto.content())
            .category(noteDto.category())
            .tag(noteDto.tag())
            .colleagueId(colleagueDto.id())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    ReviewNote saved = noteRepository.save(note);
    return new ReviewNoteDto(
        saved.getContent(),
        saved.getCategory(),
        saved.getTag(),
        colleagueDto,
        saved.getCreatedAt(),
        saved.getUpdatedAt());
  }

  @Override
  public ColleagueSummaryDto getSummaryForColleague(String colleagueName) {
    Optional<ColleagueDto> colleague = colleagueService.findByName(colleagueName);
    if (colleague.isEmpty()) {
      return new ColleagueSummaryDto(colleagueName, 0, 0, 0, Map.of());
    }
    List<ReviewNote> notes = noteRepository.findByColleagueId(colleague.get().id());
    return buildSummary(colleagueName, notes);
  }

  @Override
  public List<ColleagueSummaryDto> getSummaryForAll(LocalDate from, LocalDate to) {
    List<ReviewNote> notes =
        noteRepository.findByCreatedAtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    Map<Long, List<ReviewNote>> byId =
        notes.stream().collect(Collectors.groupingBy(ReviewNote::getColleagueId));
    return byId.entrySet().stream()
        .map(
            e -> {
              String name =
                  colleagueService.findById(e.getKey()).map(ColleagueDto::name).orElse("Unknown");
              return buildSummary(name, e.getValue());
            })
        .toList();
  }

  private ColleagueSummaryDto buildSummary(String colleagueName, List<ReviewNote> notes) {
    int highlights = (int) notes.stream().filter(n -> n.getTag() == NoteTag.HIGHLIGHT).count();
    int improvements = (int) notes.stream().filter(n -> n.getTag() == NoteTag.IMPROVEMENT).count();
    Map<NoteCategory, List<ReviewNoteDto>> notesByCategory =
        notes.stream()
            .collect(
                Collectors.groupingBy(
                    ReviewNote::getCategory,
                    Collectors.mapping(this::mapNoteToDto, Collectors.toList())));
    return new ColleagueSummaryDto(
        colleagueName, notes.size(), highlights, improvements, notesByCategory);
  }

  private ReviewNoteDto mapNoteToDto(ReviewNote note) {
    return new ReviewNoteDto(
        note.getContent(),
        note.getCategory(),
        note.getTag(),
        null,
        note.getCreatedAt(),
        note.getUpdatedAt());
  }
}
