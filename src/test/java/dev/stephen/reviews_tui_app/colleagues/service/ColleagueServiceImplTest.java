package dev.stephen.reviews_tui_app.colleagues.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;
import dev.stephen.reviews_tui_app.colleagues.entity.Colleague;
import dev.stephen.reviews_tui_app.colleagues.repository.ColleagueRepository;

@ExtendWith(MockitoExtension.class)
class ColleagueServiceImplTest {

    @Mock private ColleagueRepository colleagueRepository;

    @InjectMocks private ColleagueServiceImpl service;

    private Colleague alice;
    private Colleague bob;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        alice =
                Colleague.builder()
                        .id(1L)
                        .name("Alice Smith")
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
        bob = Colleague.builder().id(2L).name("Bob Jones").createdAt(now).updatedAt(now).build();
    }

    // --- createColleague ---

    @Test
    void createColleague_savesEntityWithName() {
        when(colleagueRepository.save(any())).thenReturn(alice);

        service.createColleague("Alice Smith");

        ArgumentCaptor<Colleague> captor = ArgumentCaptor.forClass(Colleague.class);
        verify(colleagueRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Alice Smith");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void createColleague_returnsMappedDto() {
        when(colleagueRepository.save(any())).thenReturn(alice);

        ColleagueDto result = service.createColleague("Alice Smith");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Alice Smith");
        assertThat(result.createdAt()).isEqualTo(alice.getCreatedAt());
    }

    // --- listColleagues ---

    @Test
    void listColleagues_returnsAllMappedDtos() {
        when(colleagueRepository.findAll()).thenReturn(List.of(alice, bob));

        List<ColleagueDto> result = service.listColleagues();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ColleagueDto::name)
                .containsExactly("Alice Smith", "Bob Jones");
        assertThat(result).extracting(ColleagueDto::id).containsExactly(1L, 2L);
    }

    @Test
    void listColleagues_emptyRepository_returnsEmptyList() {
        when(colleagueRepository.findAll()).thenReturn(List.of());

        assertThat(service.listColleagues()).isEmpty();
    }

    // --- findById ---

    @Test
    void findById_found_returnsPresentOptional() {
        when(colleagueRepository.findById(1L)).thenReturn(Optional.of(alice));

        Optional<ColleagueDto> result = service.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().name()).isEqualTo("Alice Smith");
    }

    @Test
    void findById_notFound_returnsEmptyOptional() {
        when(colleagueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.findById(99L)).isEmpty();
    }

    // --- findByNameFuzzy ---

    @Test
    void findByNameFuzzy_substringMatch_returnsSubstringResults() {
        when(colleagueRepository.findByNameContainingIgnoreCase("alice"))
                .thenReturn(List.of(alice));

        List<ColleagueDto> result = service.findByNameFuzzy("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alice Smith");
        verify(colleagueRepository, never()).findAll();
    }

    @Test
    void findByNameFuzzy_noSubstringMatch_fallsBackToFuzzy() {
        // "Alise" is a typo for "Alice" — Jaro-Winkler should score above 0.6
        when(colleagueRepository.findByNameContainingIgnoreCase("Alise")).thenReturn(List.of());
        when(colleagueRepository.findAll()).thenReturn(List.of(alice, bob));

        List<ColleagueDto> result = service.findByNameFuzzy("Alise");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).name()).isEqualTo("Alice Smith");
    }

    @Test
    void findByNameFuzzy_noSubstringMatch_sortsResultsByScoreDescending() {
        // Both "Alice Smith" and "Alise Smith" should match "Alise" but at different scores
        Colleague alise =
                Colleague.builder()
                        .id(3L)
                        .name("Alise Smith")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
        when(colleagueRepository.findByNameContainingIgnoreCase("Alise")).thenReturn(List.of());
        when(colleagueRepository.findAll()).thenReturn(List.of(alice, alise));

        List<ColleagueDto> result = service.findByNameFuzzy("Alise");

        // "Alise Smith" is an exact match so should score higher than "Alice Smith"
        assertThat(result.get(0).name()).isEqualTo("Alise Smith");
    }

    @Test
    void findByNameFuzzy_noMatchAtAll_returnsEmptyList() {
        when(colleagueRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());
        when(colleagueRepository.findAll()).thenReturn(List.of(alice, bob));

        List<ColleagueDto> result = service.findByNameFuzzy("xyz");

        assertThat(result).isEmpty();
    }
}
