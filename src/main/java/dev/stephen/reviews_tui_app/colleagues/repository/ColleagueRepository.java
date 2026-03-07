package dev.stephen.reviews_tui_app.colleagues.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.stephen.reviews_tui_app.colleagues.entity.Colleague;

@Repository
public interface ColleagueRepository extends JpaRepository<Colleague, Long> {
  Optional<Colleague> findByName(String name);

  List<Colleague> findByNameContainingIgnoreCase(String name);
}
