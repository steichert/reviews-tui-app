package dev.stephen.reviews_tui_app.notes.repository;

import dev.stephen.reviews_tui_app.notes.entity.ReviewNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<ReviewNote, Long> {
    List<ReviewNote> findByColleague_Name(String name);
    List<ReviewNote> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
