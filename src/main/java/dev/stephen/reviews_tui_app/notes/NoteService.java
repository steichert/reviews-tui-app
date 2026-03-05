package dev.stephen.reviews_tui_app.notes;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;

import java.time.LocalDate;
import java.util.List;

public interface NoteService {
    ReviewNoteDto createNote(ReviewNoteDto noteDto, ColleagueDto colleagueDto);
    ColleagueSummaryDto getSummaryForColleague(String colleagueName);
    List<ColleagueSummaryDto> getSummaryForAll(LocalDate from, LocalDate to);
}
