package dev.stephen.reviews_tui_app.colleagues;

import java.util.List;
import java.util.Optional;

public interface ColleagueService {
    ColleagueDto createColleague(String name);

    List<ColleagueDto> listColleagues();

    Optional<ColleagueDto> findById(Long id);

    Optional<ColleagueDto> findByName(String name);

    List<ColleagueDto> findByNameFuzzy(String query);
}
