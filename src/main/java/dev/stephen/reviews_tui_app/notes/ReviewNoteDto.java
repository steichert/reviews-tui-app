package dev.stephen.reviews_tui_app.notes;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;

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
