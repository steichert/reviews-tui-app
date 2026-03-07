# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the application (interactive shell)
./mvnw spring-boot:run

# Build (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ReviewControllerIntegrationTest

# Native build (requires GraalVM 25+)
./mvnw native:compile -Pnative
```

## Architecture

This is a **Spring Boot 4 CLI application** using Spring Shell for an interactive terminal. It tracks performance notes about colleagues. The database is a file-based H2 instance stored at `~/.reviews-tui-app/data/notesdb`.

### Module Structure (Spring Modulith)

The app is organized into four modules enforced by Spring Modulith. Cross-module communication must go through public service interfaces only — repositories are package-private.

- **`colleagues/`** — Colleague entity management. Public API: `ColleagueService` interface (at module root). Sub-packages: `entity/`, `service/`, `repository/`, `dto/`.
- **`notes/`** — Performance note management. Public API: `NoteService` interface (at module root). Notes have a `NoteCategory` (`TECHNICAL_ABILITY`, `RESPONSIBILITY_TO_OTHERS`, `CUSTOMER_SUCCESS`, `GENERAL`) and `NoteTag` (`HIGHLIGHT`, `IMPROVEMENT`, `NONE`). Sub-packages: `entity/`, `service/`, `repository/`.
- **`gateway/`** — External adapters only: `cli/ReviewCommands.java` (Spring Shell commands) and `rest/ReviewController.java` (REST API). These depend on the service interfaces, never directly on repositories.
- **`shared/`** — Cross-cutting Spring configuration (`AppConfiguration.java`).

DTOs and enums that form the public module API live at the module root package (e.g., `colleagues/ColleagueDto.java`, not `colleagues/dto/ColleagueDto.java`).

### Data Flow

```
CLI (ReviewCommands) ──→ ColleagueService / NoteService ──→ JPA Repositories ──→ H2
REST (ReviewController) ──→ same services
```

### REST API

Base path: `/api/v1`

- `POST /colleagues` — create a colleague
- `GET /colleagues` — list all colleagues
- `GET /colleagues/search?q={query}` — fuzzy name search (uses Apache Commons Text)
- `POST /notes` — create a note (`colleagueId`, `content`, `category`, `tag`)
- `GET /notes/summary?colleague={name}` — summary for one colleague
- `GET /notes/summary?from={date}&to={date}` — summaries for all colleagues in date range

SpringDoc OpenAPI UI is available at `/swagger-ui.html` when running.

### Key Conventions

- DTOs are Java records (e.g., `ColleagueDto`, `ReviewNoteDto`).
- Entities use Lombok (`@Data`, `@Builder`, etc.) and JPA auditing (`createdAt`, `updatedAt`).
- Spring Modulith auto-generates PlantUML architecture diagrams to `target/spring-modulith-docs/` during test runs.
- Spring AI (Anthropic) is a dependency — the app may integrate LLM summarization for the `summary` command.
- H2 console is accessible at `http://localhost:8080/h2-console` for debugging (requires `spring.main.web-application-type` to not be `none`).
- TamboUI (`dev.tamboui`) is a terminal UI DSL dependency used for rendering TUI components.

### Planned CLI Commands (per README)

`add-note`, `list-notes --colleague "Name" [--category] [--tag]`, `summary --colleague "Name"`, `list-colleagues`, `exit`

## Spring Boot 4 / Java Notes

- **Jackson 3.x**: packages moved from `com.fasterxml.jackson.*` to `tools.jackson.*`. Use `tools.jackson.databind.json.JsonMapper` — do not autowire `com.fasterxml.jackson.databind.ObjectMapper`.
- **`@AutoConfigureMockMvc`**: now from `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` (requires `spring-boot-starter-webmvc-test` test dependency).

## Test Configuration

Integration tests require these property overrides to avoid blocking/errors:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc  // org.springframework.boot.webmvc.test.autoconfigure
@TestPropertySource(properties = {
    "spring.shell.interactive.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.autoconfigure.exclude=org.springframework.shell.core.autoconfigure.SpringShellAutoConfiguration"
})
```

- Spring Shell blocks in non-interactive environments unless `spring.shell.interactive.enabled=false`.
- **Spring Shell 4.0.1 bug**: `spring-shell-starter-test` removes jline TUI classes from the test classpath, but `ThemingAutoConfiguration` (loaded transitively via `JLineShellAutoConfiguration`) still references `org.springframework.shell.jline.tui.style.ThemeActive` and crashes context startup. Excluding `ThemingAutoConfiguration` directly via `spring.autoconfigure.exclude` does NOT work (it's not in `AutoConfiguration.imports`). Must exclude the root `SpringShellAutoConfiguration` instead.
- `src/test/resources/application.yaml` uses a relative H2 path that H2 2.4+ rejects — always override with an in-memory URL in integration tests.
- Inject repositories directly for `@BeforeEach` cleanup; delete notes before colleagues (FK constraint).
- Spring Modulith boundaries are only enforced in `@ApplicationModuleTest`, not `@SpringBootTest` — repositories can be `@Autowired` freely in `@SpringBootTest` tests.
