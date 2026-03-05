# 📒 Colleague Notes CLI

A Spring Boot CLI tool for capturing short performance notes about colleagues — building up a picture of their achievements and areas for growth over time.

---

## Tech Stack

| Concern          | Choice                                  |
|------------------|-----------------------------------------|
| Language         | Java 25                                 |
| Framework        | Spring Boot 4                           |
| Architecture     | Spring Modulith                         |
| CLI              | Spring Shell 3.x (interactive flow)     |
| Persistence      | H2 file-based database (local disk)     |
| ORM              | Spring Data JPA / Hibernate             |

---

## Prerequisites

- **Java 25** (`java -version` should show 25)
- **Maven 3.9+**

---

## Running the Application

```bash
# Clone / download the project, then:
cd reviews-tui-app

# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

The H2 database will be persisted automatically at:
```
~/.reviews-tui-app/data/notesdb.mv.db
```
It survives application restarts — your notes are safe.

---

## CLI Commands

Once the shell starts, use the following commands:

### `add-note` — Add a performance note (interactive guided flow)

```
shell:> add-note
```

You will be guided step-by-step through:
1. **Colleague name** — Type a name. If they don't exist yet, they are created automatically.
2. **Performance category** — Select one from:
    - Technical Ability
    - Responsibility to Others
    - Client Success
    - General
3. **Note content** — Free-text description of the observation.
4. **Tag** — Classify the note as:
    - ⭐ Highlight — a positive achievement to celebrate
    - 🔧 Improvement — an area needing development
    - No tag — neutral observation

---

### `list-notes` — View notes for a colleague

```
shell:> list-notes --colleague "Alice Smith"

# Filter by category:
shell:> list-notes --colleague "Alice Smith" --category TECHNICAL_ABILITY

# Filter by tag:
shell:> list-notes --colleague "Alice Smith" --tag HIGHLIGHT
```

Available category values: `TECHNICAL_ABILITY`, `RESPONSIBILITY_TO_OTHERS`, `CLIENT_SUCCESS`, `GENERAL`

Available tag values: `HIGHLIGHT`, `IMPROVEMENT`, `NONE`

---

### `summary` — Performance summary view

Shows all notes grouped by category with tag indicators:

```
shell:> summary --colleague "Alice Smith"
```

Output includes a count of total notes, highlights, and improvement areas.

---

### `list-colleagues` — View all tracked colleagues

```
shell:> list-colleagues
```

---

### `help` — Show all available commands

```
shell:> help
```

### `exit` / `quit` — Exit the shell

```
shell:> exit
```

---

## Project Structure (Spring Modulith)

```
src/main/java/com/notes/colleaguenotes/
│
├── ColleagueNotesApplication.java     # Entry point
│
├── colleagues/                        # Module: Colleague management
│   ├── Colleague.java                 #   Entity
│   ├── ColleagueRepository.java       #   Repository (package-private)
│   └── ColleagueService.java          #   Public service API
│
├── notes/                             # Module: Performance notes
│   ├── PerformanceNote.java           #   Entity
│   ├── PerformanceCategory.java       #   Enum: 4 categories
│   ├── NoteTag.java                   #   Enum: Highlight / Improvement / None
│   ├── NoteRepository.java            #   Repository (package-private)
│   ├── NoteService.java               #   Public service API
│   └── NoteCommands.java              #   Spring Shell CLI commands
│
└── shared/                            # Module: Cross-cutting config
    └── AppConfiguration.java          #   Banner + app-level beans
```

Spring Modulith enforces that modules only interact via **public service APIs**, keeping the architecture clean. Run the included `ModularityTests` to verify compliance.

---

## Running Tests

```bash
./mvnw test
```

Tests use an **in-memory H2** instance (no file I/O), so they're fast and isolated.

The `ModularityTests` class also generates **PlantUML architecture diagrams** into `target/spring-modulith-docs/`.

---

## Database Access (Optional Debug)

The H2 console is available during runtime at `http://localhost:8080/h2-console` with:
- **JDBC URL:** `jdbc:h2:file:~/.reviews-tui-app/data/notesdb`
- **Username:** `sa`
- **Password:** *(empty)*

> Note: Disable `spring.main.web-application-type=none` temporarily in `application.properties` to access the web console.
