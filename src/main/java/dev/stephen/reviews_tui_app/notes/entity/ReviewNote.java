package dev.stephen.reviews_tui_app.notes.entity;

import dev.stephen.reviews_tui_app.colleagues.entity.Colleague;
import dev.stephen.reviews_tui_app.notes.model.NoteCategory;
import dev.stephen.reviews_tui_app.notes.model.NoteTag;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "colleague_id", insertable = false, updatable = false)
    private Colleague colleague;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;
}
