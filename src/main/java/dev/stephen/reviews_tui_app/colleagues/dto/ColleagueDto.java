package dev.stephen.reviews_tui_app.colleagues.dto;

import java.time.LocalDateTime;

public record ColleagueDto(
        Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {}
