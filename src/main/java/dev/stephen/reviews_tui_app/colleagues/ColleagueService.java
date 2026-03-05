package dev.stephen.reviews_tui_app.colleagues;

import dev.stephen.reviews_tui_app.colleagues.dto.ColleagueDto;

import java.util.List;
import java.util.Optional;

public interface ColleagueService {
    ColleagueDto createColleague(String name);
    List<ColleagueDto> listColleagues();
    Optional<ColleagueDto> findById(Long id);
    List<ColleagueDto> findByNameFuzzy(String query);
}
