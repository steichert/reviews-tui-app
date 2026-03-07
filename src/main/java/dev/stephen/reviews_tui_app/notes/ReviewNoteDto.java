package dev.stephen.reviews_tui_app.notes;

import java.time.LocalDateTime;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;

public record ReviewNoteDto(
    String content,
    NoteCategory category,
    NoteTag tag,
    ColleagueDto colleague,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
