package dev.stephen.reviews_tui_app.notes.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import dev.stephen.reviews_tui_app.notes.NoteCategory;
import dev.stephen.reviews_tui_app.notes.NoteTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "review_notes")
public class ReviewNote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "VARCHAR(50)")
  private NoteCategory category;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "VARCHAR(50)")
  private NoteTag tag;

  @Column(name = "colleague_id", nullable = false)
  private Long colleagueId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false, updatable = false)
  private LocalDateTime updatedAt;
}
