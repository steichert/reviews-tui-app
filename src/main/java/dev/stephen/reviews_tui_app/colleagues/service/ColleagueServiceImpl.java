package dev.stephen.reviews_tui_app.colleagues.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;
import dev.stephen.reviews_tui_app.colleagues.ColleagueService;
import dev.stephen.reviews_tui_app.colleagues.entity.Colleague;
import dev.stephen.reviews_tui_app.colleagues.repository.ColleagueRepository;

@Service
public class ColleagueServiceImpl implements ColleagueService {

  private final ColleagueRepository colleagueRepository;
  private final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();

  public ColleagueServiceImpl(ColleagueRepository colleagueRepository) {
    this.colleagueRepository = colleagueRepository;
  }

  @Override
  public ColleagueDto createColleague(String name) {
    Colleague colleague =
        Colleague.builder()
            .name(name)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    return toDto(colleagueRepository.save(colleague));
  }

  @Override
  public List<ColleagueDto> listColleagues() {
    return colleagueRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  public Optional<ColleagueDto> findById(Long id) {
    return colleagueRepository.findById(id).map(this::toDto);
  }

  @Override
  public Optional<ColleagueDto> findByName(String name) {
    return colleagueRepository.findByName(name).map(this::toDto);
  }

  @Override
  public List<ColleagueDto> findByNameFuzzy(String query) {
    List<Colleague> substringMatches = colleagueRepository.findByNameContainingIgnoreCase(query);
    if (!substringMatches.isEmpty()) {
      return substringMatches.stream().map(this::toDto).toList();
    }
    double threshold = 0.6;
    String lowerQuery = query.toLowerCase();
    return colleagueRepository.findAll().stream()
        .filter(
            c -> jaroWinklerSimilarity.apply(c.getName().toLowerCase(), lowerQuery) >= threshold)
        .sorted(
            (a, b) ->
                Double.compare(
                    jaroWinklerSimilarity.apply(b.getName().toLowerCase(), lowerQuery),
                    jaroWinklerSimilarity.apply(a.getName().toLowerCase(), lowerQuery)))
        .map(this::toDto)
        .toList();
  }

  private ColleagueDto toDto(Colleague colleague) {
    return new ColleagueDto(
        colleague.getId(), colleague.getName(), colleague.getCreatedAt(), colleague.getUpdatedAt());
  }
}
