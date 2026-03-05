package dev.stephen.reviews_tui_app.gateway.rest.dto;

import dev.stephen.reviews_tui_app.notes.model.NoteCategory;
import dev.stephen.reviews_tui_app.notes.model.NoteTag;

public record CreateNoteRequest(Long colleagueId, String content, NoteCategory category, NoteTag tag) {
}
