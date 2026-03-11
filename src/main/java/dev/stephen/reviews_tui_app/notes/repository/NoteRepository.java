package dev.stephen.reviews_tui_app.notes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.stephen.reviews_tui_app.notes.entity.ReviewNote;

@Repository
public interface NoteRepository extends JpaRepository<ReviewNote, Long> {
    List<ReviewNote> findByColleagueId(Long colleagueId);

    List<ReviewNote> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
