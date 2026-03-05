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
./mvnw test -Dtest=ReviewsTuiAppApplicationTests

# Native build (requires GraalVM 25+)
./mvnw native:compile -Pnative
```

## Architecture

This is a **Spring Boot CLI application** using Spring Shell for an interactive terminal. It tracks performance notes about colleagues. The database is a file-based H2 instance stored at `~/Development/.reviews-tui-app/data/notesdb`.

### Module Structure (Spring Modulith)

The app is organized into four modules enforced by Spring Modulith. Cross-module communication must go through public service interfaces only — repositories are package-private.

- **`colleagues/`** — Colleague entity management. Public API: `ColleagueService` interface.
- **`notes/`** — Performance note management. Public API: `NoteService` interface. Notes have a `NoteCategory` (TECHNICAL_ABILITY, RESPONSIBILITY_TO_OTHERS, CUSTOMER_SUCCESS, GENERAL) and `NoteTag` (HIGHLIGHT, IMPROVEMENT, NONE).
- **`gateway/`** — External adapters only: `cli/ReviewCommands.java` (Spring Shell commands) and `rest/ReviewController.java` (REST API). These depend on the service interfaces, never directly on repositories.
- **`shared/`** — Cross-cutting Spring configuration (`AppConfiguration.java`).

### Data Flow

```
CLI (ReviewCommands) ──→ ColleagueService / NoteService ──→ JPA Repositories ──→ H2
REST (ReviewController) ──→ same services
```

### Key Conventions

- DTOs are Java records (e.g., `ColleagueDto`, `ReviewNoteDto`).
- Entities use Lombok (`@Data`, `@Builder`, etc.) and JPA auditing (`createdAt`, `updatedAt`).
- Spring Modulith auto-generates PlantUML architecture diagrams to `target/spring-modulith-docs/` during test runs.
- Spring AI (Anthropic) is a dependency — the app may integrate LLM summarization for the `summary` command.
- H2 console is accessible at `http://localhost:8080/h2-console` for debugging (requires `spring.main.web-application-type` to not be `none`).

### Planned CLI Commands (per README)

`add-note`, `list-notes --colleague "Name" [--category] [--tag]`, `summary --colleague "Name"`, `list-colleagues`, `exit`
