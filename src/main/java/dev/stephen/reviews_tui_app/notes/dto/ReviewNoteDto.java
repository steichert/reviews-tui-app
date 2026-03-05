package dev.stephen.reviews_tui_app.notes.dto;

import dev.stephen.reviews_tui_app.colleagues.dto.ColleagueDto;
import dev.stephen.reviews_tui_app.notes.model.NoteCategory;
import dev.stephen.reviews_tui_app.notes.model.NoteTag;

import java.time.LocalDateTime;

public record ReviewNoteDto(
        String content,
        NoteCategory category,
        NoteTag tag,
        ColleagueDto colleague,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
