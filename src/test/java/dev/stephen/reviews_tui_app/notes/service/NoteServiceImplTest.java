package dev.stephen.reviews_tui_app.notes.service;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;
import dev.stephen.reviews_tui_app.colleagues.ColleagueService;
import dev.stephen.reviews_tui_app.notes.ColleagueSummaryDto;
import dev.stephen.reviews_tui_app.notes.NoteCategory;
import dev.stephen.reviews_tui_app.notes.NoteTag;
import dev.stephen.reviews_tui_app.notes.ReviewNoteDto;
import dev.stephen.reviews_tui_app.notes.entity.ReviewNote;
import dev.stephen.reviews_tui_app.notes.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ColleagueService colleagueService;

    @InjectMocks
    private NoteServiceImpl service;

    private ColleagueDto aliceDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        aliceDto = new ColleagueDto(1L, "Alice Smith", now, now);
    }

    private ReviewNote buildNote(Long id, String content, NoteCategory category, NoteTag tag, Long colleagueId) {
        return ReviewNote.builder()
                .id(id)
                .content(content)
                .category(category)
                .tag(tag)
                .colleagueId(colleagueId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- createNote ---

    @Test
    void createNote_savesEntityWithCorrectFields() {
        ReviewNoteDto input = new ReviewNoteDto("Great work", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT, null, null, null);
        ReviewNote saved = buildNote(10L, "Great work", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT, 1L);
        when(noteRepository.save(any())).thenReturn(saved);

        service.createNote(input, aliceDto);

        ArgumentCaptor<ReviewNote> captor = ArgumentCaptor.forClass(ReviewNote.class);
        verify(noteRepository).save(captor.capture());
        ReviewNote captured = captor.getValue();
        assertThat(captured.getContent()).isEqualTo("Great work");
        assertThat(captured.getCategory()).isEqualTo(NoteCategory.TECHNICAL_ABILITY);
        assertThat(captured.getTag()).isEqualTo(NoteTag.HIGHLIGHT);
        assertThat(captured.getColleagueId()).isEqualTo(1L);
        assertThat(captured.getCreatedAt()).isNotNull();
    }

    @Test
    void createNote_returnsDto_withSavedFieldsAndColleague() {
        ReviewNoteDto input = new ReviewNoteDto("Great work", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT, null, null, null);
        LocalDateTime savedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        ReviewNote saved = ReviewNote.builder()
                .id(10L).content("Great work").category(NoteCategory.TECHNICAL_ABILITY).tag(NoteTag.HIGHLIGHT)
                .colleagueId(1L).createdAt(savedAt).updatedAt(savedAt).build();
        when(noteRepository.save(any())).thenReturn(saved);

        ReviewNoteDto result = service.createNote(input, aliceDto);

        assertThat(result.content()).isEqualTo("Great work");
        assertThat(result.category()).isEqualTo(NoteCategory.TECHNICAL_ABILITY);
        assertThat(result.tag()).isEqualTo(NoteTag.HIGHLIGHT);
        assertThat(result.colleague()).isEqualTo(aliceDto);
        assertThat(result.createdAt()).isEqualTo(savedAt);
    }

    // --- getSummaryForColleague ---

    @Test
    void getSummaryForColleague_countsHighlightsAndImprovements() {
        List<ReviewNote> notes = List.of(
                buildNote(1L, "Good", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT, 1L),
                buildNote(2L, "Needs work", NoteCategory.GENERAL, NoteTag.IMPROVEMENT, 1L),
                buildNote(3L, "Neutral", NoteCategory.GENERAL, NoteTag.NONE, 1L)
        );
        when(colleagueService.findByName("Alice Smith")).thenReturn(Optional.of(aliceDto));
        when(noteRepository.findByColleagueId(1L)).thenReturn(notes);

        ColleagueSummaryDto summary = service.getSummaryForColleague("Alice Smith");

        assertThat(summary.colleagueName()).isEqualTo("Alice Smith");
        assertThat(summary.totalNotes()).isEqualTo(3);
        assertThat(summary.highlights()).isEqualTo(1);
        assertThat(summary.improvements()).isEqualTo(1);
    }

    @Test
    void getSummaryForColleague_groupsNotesByCategory() {
        List<ReviewNote> notes = List.of(
                buildNote(1L, "Tech note", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT, 1L),
                buildNote(2L, "General note", NoteCategory.GENERAL, NoteTag.NONE, 1L)
        );
        when(colleagueService.findByName("Alice Smith")).thenReturn(Optional.of(aliceDto));
        when(noteRepository.findByColleagueId(1L)).thenReturn(notes);

        ColleagueSummaryDto summary = service.getSummaryForColleague("Alice Smith");

        assertThat(summary.notesByCategory()).containsKey(NoteCategory.TECHNICAL_ABILITY);
        assertThat(summary.notesByCategory()).containsKey(NoteCategory.GENERAL);
        assertThat(summary.notesByCategory().get(NoteCategory.TECHNICAL_ABILITY)).hasSize(1);
        assertThat(summary.notesByCategory().get(NoteCategory.GENERAL)).hasSize(1);
    }

    @Test
    void getSummaryForColleague_noNotes_returnsZeroCounts() {
        when(colleagueService.findByName("Alice Smith")).thenReturn(Optional.of(aliceDto));
        when(noteRepository.findByColleagueId(1L)).thenReturn(List.of());

        ColleagueSummaryDto summary = service.getSummaryForColleague("Alice Smith");

        assertThat(summary.totalNotes()).isEqualTo(0);
        assertThat(summary.highlights()).isEqualTo(0);
        assertThat(summary.improvements()).isEqualTo(0);
        assertThat(summary.notesByCategory()).isEmpty();
    }

    // --- getSummaryForAll ---

    @Test
    void getSummaryForAll_groupsNotesByColleague() {
        ColleagueDto bobDto = new ColleagueDto(2L, "Bob Jones", LocalDateTime.now(), LocalDateTime.now());
        List<ReviewNote> notes = List.of(
                buildNote(1L, "Alice note", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT, 1L),
                buildNote(2L, "Bob note", NoteCategory.GENERAL, NoteTag.NONE, 2L)
        );
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(noteRepository.findByCreatedAtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX))).thenReturn(notes);
        when(colleagueService.findById(1L)).thenReturn(Optional.of(aliceDto));
        when(colleagueService.findById(2L)).thenReturn(Optional.of(bobDto));

        List<ColleagueSummaryDto> summaries = service.getSummaryForAll(from, to);

        assertThat(summaries).hasSize(2);
        assertThat(summaries).extracting(ColleagueSummaryDto::colleagueName)
                .containsExactlyInAnyOrder("Alice Smith", "Bob Jones");
    }

    @Test
    void getSummaryForAll_correctCountsPerColleague() {
        List<ReviewNote> notes = List.of(
                buildNote(1L, "Note 1", NoteCategory.TECHNICAL_ABILITY, NoteTag.HIGHLIGHT, 1L),
                buildNote(2L, "Note 2", NoteCategory.GENERAL, NoteTag.IMPROVEMENT, 1L)
        );
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(noteRepository.findByCreatedAtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX))).thenReturn(notes);
        when(colleagueService.findById(1L)).thenReturn(Optional.of(aliceDto));

        List<ColleagueSummaryDto> summaries = service.getSummaryForAll(from, to);

        ColleagueSummaryDto aliceSummary = summaries.get(0);
        assertThat(aliceSummary.totalNotes()).isEqualTo(2);
        assertThat(aliceSummary.highlights()).isEqualTo(1);
        assertThat(aliceSummary.improvements()).isEqualTo(1);
    }

    @Test
    void getSummaryForAll_skipsNoteWhoseColleagueCannotBeResolved() {
        ReviewNote orphan = ReviewNote.builder()
                .id(99L).content("Orphan").category(NoteCategory.GENERAL).tag(NoteTag.NONE)
                .colleagueId(99L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(noteRepository.findByCreatedAtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX)))
                .thenReturn(List.of(orphan));
        when(colleagueService.findById(99L)).thenReturn(Optional.empty());

        List<ColleagueSummaryDto> summaries = service.getSummaryForAll(from, to);

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).colleagueName()).isEqualTo("Unknown");
    }

    @Test
    void getSummaryForAll_passesCorrectDateRangeToRepository() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);
        when(noteRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        service.getSummaryForAll(from, to);

        verify(noteRepository).findByCreatedAtBetween(
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999_999_999));
    }
}
