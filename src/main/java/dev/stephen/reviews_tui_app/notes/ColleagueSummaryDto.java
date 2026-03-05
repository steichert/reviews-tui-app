package dev.stephen.reviews_tui_app.notes;

import dev.stephen.reviews_tui_app.notes.dto.ReviewNoteDto;
import dev.stephen.reviews_tui_app.notes.model.NoteCategory;

import java.util.List;
import java.util.Map;

public record ColleagueSummaryDto(
        String colleagueName,
        int totalNotes,
        int highlights,
        int improvements,
        Map<NoteCategory, List<ReviewNoteDto>> notesByCategory
) {}
