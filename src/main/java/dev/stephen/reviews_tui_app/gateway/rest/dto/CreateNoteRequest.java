package dev.stephen.reviews_tui_app.gateway.rest.dto;

import dev.stephen.reviews_tui_app.notes.NoteCategory;
import dev.stephen.reviews_tui_app.notes.NoteTag;

public record CreateNoteRequest(
    Long colleagueId, String content, NoteCategory category, NoteTag tag) {}
