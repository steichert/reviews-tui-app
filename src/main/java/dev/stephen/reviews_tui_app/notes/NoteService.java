package dev.stephen.reviews_tui_app.notes;

import java.time.LocalDate;
import java.util.List;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;

public interface NoteService {
    ReviewNoteDto createNote(ReviewNoteDto noteDto, ColleagueDto colleagueDto);

    ColleagueSummaryDto getSummaryForColleague(String colleagueName);

    List<ColleagueSummaryDto> getSummaryForAll(LocalDate from, LocalDate to);
}
