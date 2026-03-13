package dev.stephen.reviews_tui_app;

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.form;
import static dev.tamboui.toolkit.Toolkit.handleTextInputKey;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;
import static dev.tamboui.toolkit.Toolkit.textInput;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import dev.stephen.reviews_tui_app.colleagues.ColleagueDto;
import dev.stephen.reviews_tui_app.colleagues.ColleagueService;
import dev.stephen.reviews_tui_app.notes.NoteCategory;
import dev.stephen.reviews_tui_app.notes.NoteService;
import dev.stephen.reviews_tui_app.notes.NoteTag;
import dev.stephen.reviews_tui_app.notes.ReviewNoteDto;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.event.KeyEvent;
import dev.tamboui.widgets.form.FormState;
import dev.tamboui.widgets.input.TextInputState;

@SpringBootApplication
public class Application extends ToolkitApp implements ApplicationRunner {

    private enum Screen {
        WELCOME,
        CAPTURE_NOTE
    }

    private enum NoteSection {
        CATEGORY,
        TAG,
        CONTENT
    }

    @Autowired private ColleagueService colleagueService;
    @Autowired private NoteService noteService;

    private Screen currentScreen = Screen.WELCOME;
    private ColleagueDto selectedColleague = null;
    private int welcomeSelectedIndex = 0;
    private boolean addingColleague = false;
    private int categoryCursorIndex = 0;
    private int categoryToggledIndex = -1;
    private int tagCursorIndex = 0;
    private int tagToggledIndex = -1;
    private FormState addColleagueFormState;
    private TextInputState noteInputState;
    private List<ColleagueDto> colleagues = List.of();
    private String statusMessage = "";
    private boolean statusIsError = false;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        super.run();
    }

    @Override
    protected void onStart() {
        colleagues = colleagueService.listColleagues();

        noteInputState = new TextInputState();

        addColleagueFormState = FormState.builder().textField("name").build();

        runner().eventRouter()
                .addGlobalHandler(
                        event -> {
                            if (!(event instanceof KeyEvent ke)) return EventResult.UNHANDLED;
                            if (ke.isCtrlC()) {
                                quit();
                                return EventResult.HANDLED;
                            }
                            if (currentScreen == Screen.CAPTURE_NOTE) {
                                return handleNoteKey(ke);
                            }
                            if (currentScreen == Screen.WELCOME) {
                                return handleWelcomeKey(ke);
                            }
                            return EventResult.UNHANDLED;
                        });
    }

    private EventResult handleWelcomeKey(KeyEvent ke) {
        int total = colleagues.size() + 1;

        if (!addingColleague) {
            if (ke.isUp()) {
                runner().runOnRenderThread(
                                () -> welcomeSelectedIndex = Math.max(0, welcomeSelectedIndex - 1));
                return EventResult.HANDLED;
            }
            if (ke.isDown()) {
                runner().runOnRenderThread(
                                () ->
                                        welcomeSelectedIndex =
                                                Math.min(total - 1, welcomeSelectedIndex + 1));
                return EventResult.HANDLED;
            }
            if (ke.isConfirm()) {
                runner().runOnRenderThread(
                                () -> {
                                    if (welcomeSelectedIndex < colleagues.size()) {
                                        selectedColleague = colleagues.get(welcomeSelectedIndex);
                                        prepareNoteScreen();
                                        currentScreen = Screen.CAPTURE_NOTE;
                                    } else {
                                        addingColleague = true;
                                        addColleagueFormState.textField("name").clear();
                                    }
                                });
                return EventResult.HANDLED;
            }
        } else {
            if (ke.isCancel()) {
                runner().runOnRenderThread(
                                () -> {
                                    addingColleague = false;
                                    addColleagueFormState.textField("name").clear();
                                });
                return EventResult.HANDLED;
            }
        }
        return EventResult.UNHANDLED;
    }

    private void prepareNoteScreen() {
        noteInputState.clear();
        categoryCursorIndex = 0;
        categoryToggledIndex = -1;
        tagCursorIndex = 0;
        tagToggledIndex = -1;
        statusMessage = "";
        statusIsError = false;
    }

    private void handleAddColleagueSubmit(FormState fs) {
        runner().runOnRenderThread(
                        () -> {
                            String name = fs.textValue("name").trim();
                            if (!name.isBlank()) {
                                ColleagueDto newColleague = colleagueService.createColleague(name);
                                colleagues = colleagueService.listColleagues();
                                selectedColleague = newColleague;
                                fs.textField("name").clear();
                                addingColleague = false;
                                prepareNoteScreen();
                                currentScreen = Screen.CAPTURE_NOTE;
                            }
                        });
    }

    private void handleNoteSubmit() {
        runner().runOnRenderThread(
                        () -> {
                            if (categoryToggledIndex < 0) {
                                statusMessage = "Category cannot be empty.";
                                statusIsError = true;
                                return;
                            }
                            if (tagToggledIndex < 0) {
                                statusMessage = "Tag cannot be empty.";
                                statusIsError = true;
                                return;
                            }
                            String content = noteInputState.text();
                            if (content.isBlank()) {
                                statusMessage = "Note content cannot be empty.";
                                statusIsError = true;
                                return;
                            }
                            NoteCategory category = NoteCategory.values()[categoryToggledIndex];
                            NoteTag tag = NoteTag.values()[tagToggledIndex];
                            try {
                                noteService.createNote(
                                        new ReviewNoteDto(content, category, tag, null, null, null),
                                        selectedColleague);
                                statusMessage = "Note saved for " + selectedColleague.name();
                                statusIsError = false;
                                noteInputState.clear();
                            } catch (Exception e) {
                                statusMessage = "Error: " + e.getMessage();
                                statusIsError = true;
                            }
                        });
    }

    @Override
    protected Element render() {
        return switch (currentScreen) {
            case WELCOME -> renderWelcomeScreen();
            case CAPTURE_NOTE -> renderNoteScreen();
        };
    }

    private Element renderWelcomeScreen() {
        Element banner =
                column(
                        text(" ____  _______     _____ _______        _____ _    _ ___ "),
                        text("|  _ \\| ____\\ \\   / /_ _| ____\\ \\      / /_ _| |  | |_ _|"),
                        text("| |_) |  _|  \\ \\ / / | ||  _|  \\ \\ /\\ / / | || |  | || |"),
                        text("|  _ <| |___  \\ V /  | || |___  \\ V  V /  | || |__| || |"),
                        text("|_| \\_\\_____|  \\_/  |___|_____|  \\_/\\_/  |___|_____/|___|"));

        List<String> items = new ArrayList<>();
        for (ColleagueDto c : colleagues) items.add(c.name());
        items.add("Add Colleague");

        List<Element> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            boolean selected = (i == welcomeSelectedIndex);
            boolean isAddOption = (i == items.size() - 1);
            String label = (selected ? "  > " : "    ") + items.get(i);
            Element row;
            if (selected && isAddOption) {
                row = text(label).yellow().bold();
            } else if (isAddOption) {
                row = text(label).yellow();
            } else if (selected) {
                row = text(label).bold();
            } else {
                row = text(label);
            }
            rows.add(row);
        }

        Element listPanel =
                panel("Select a Colleague", column(rows.toArray(new Element[0]))).rounded();

        Element footer = text("  ↑↓ navigate  •  Enter to select  •  Ctrl+C to quit").dim();

        if (addingColleague) {
            return column(
                    banner,
                    spacer(1),
                    listPanel,
                    spacer(1),
                    form(addColleagueFormState)
                            .field("name", "New colleague")
                            .submitOnEnter(true)
                            .onSubmit(this::handleAddColleagueSubmit)
                            .rounded(),
                    spacer(1),
                    footer);
        }

        return column(banner, spacer(1), listPanel, spacer(1), footer);
    }

    private NoteSection currentNoteSection() {
        String id = runner().focusManager().focusedId();
        if ("category-panel".equals(id)) return NoteSection.CATEGORY;
        if ("tag-panel".equals(id)) return NoteSection.TAG;
        return NoteSection.CONTENT;
    }

    private EventResult handleNoteKey(KeyEvent ke) {
        if (ke.isCancel()) {
            runner().runOnRenderThread(() -> currentScreen = Screen.WELCOME);
            return EventResult.HANDLED;
        }
        return EventResult.UNHANDLED;
    }

    private Element renderNoteScreen() {
        NoteSection activeSection = currentNoteSection();

        Element statusLine =
                statusMessage.isBlank()
                        ? spacer(1)
                        : statusIsError
                                ? text("  " + statusMessage).red()
                                : text("  " + statusMessage).green();

        // Category toggles
        NoteCategory[] categories = NoteCategory.values();
        List<Element> categoryRows = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) {
            boolean isCursor = (i == categoryCursorIndex);
            boolean isToggled = (i == categoryToggledIndex);
            String cursor = (isCursor && activeSection == NoteSection.CATEGORY) ? "  > " : "    ";
            String checkbox = isToggled ? "[x] " : "[ ] ";
            String label = cursor + checkbox + categories[i].name();
            Element row;
            if (isCursor && activeSection == NoteSection.CATEGORY) {
                row = text(label).bold().cyan();
            } else if (isToggled) {
                row = text(label).bold().green();
            } else {
                row = text(label);
            }
            categoryRows.add(row);
        }
        Element categoryPanel =
                panel("Category", column(categoryRows.toArray(new Element[0])))
                        .rounded()
                        .id("category-panel")
                        .focusable()
                        .onKeyEvent(
                                ke -> {
                                    if (ke.isUp()) {
                                        runner().runOnRenderThread(
                                                        () ->
                                                                categoryCursorIndex =
                                                                        Math.max(
                                                                                0,
                                                                                categoryCursorIndex
                                                                                        - 1));
                                        return EventResult.HANDLED;
                                    }
                                    if (ke.isDown()) {
                                        runner().runOnRenderThread(
                                                        () ->
                                                                categoryCursorIndex =
                                                                        Math.min(
                                                                                NoteCategory
                                                                                                .values()
                                                                                                .length
                                                                                        - 1,
                                                                                categoryCursorIndex
                                                                                        + 1));
                                        return EventResult.HANDLED;
                                    }
                                    if (ke.isConfirm()) {
                                        runner().runOnRenderThread(
                                                        () ->
                                                                categoryToggledIndex =
                                                                        (categoryToggledIndex
                                                                                        == categoryCursorIndex)
                                                                                ? -1
                                                                                : categoryCursorIndex);
                                        return EventResult.HANDLED;
                                    }
                                    return EventResult.UNHANDLED;
                                });

        // Tag toggles
        NoteTag[] tags = NoteTag.values();
        List<Element> tagRows = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
            boolean isCursor = (i == tagCursorIndex);
            boolean isToggled = (i == tagToggledIndex);
            String cursor = (isCursor && activeSection == NoteSection.TAG) ? "  > " : "    ";
            String checkbox = isToggled ? "[x] " : "[ ] ";
            String label = cursor + checkbox + tags[i].name();
            Element row;
            if (isCursor && activeSection == NoteSection.TAG) {
                row = text(label).bold().cyan();
            } else if (isToggled) {
                row = text(label).bold().green();
            } else {
                row = text(label);
            }
            tagRows.add(row);
        }
        Element tagPanel =
                panel("Tag", column(tagRows.toArray(new Element[0])))
                        .rounded()
                        .id("tag-panel")
                        .focusable()
                        .onKeyEvent(
                                ke -> {
                                    if (ke.isUp()) {
                                        runner().runOnRenderThread(
                                                        () ->
                                                                tagCursorIndex =
                                                                        Math.max(
                                                                                0,
                                                                                tagCursorIndex
                                                                                        - 1));
                                        return EventResult.HANDLED;
                                    }
                                    if (ke.isDown()) {
                                        runner().runOnRenderThread(
                                                        () ->
                                                                tagCursorIndex =
                                                                        Math.min(
                                                                                NoteTag.values()
                                                                                                .length
                                                                                        - 1,
                                                                                tagCursorIndex
                                                                                        + 1));
                                        return EventResult.HANDLED;
                                    }
                                    if (ke.isConfirm()) {
                                        runner().runOnRenderThread(
                                                        () ->
                                                                tagToggledIndex =
                                                                        (tagToggledIndex
                                                                                        == tagCursorIndex)
                                                                                ? -1
                                                                                : tagCursorIndex);
                                        return EventResult.HANDLED;
                                    }
                                    return EventResult.UNHANDLED;
                                });

        return column(
                panel(
                                "Note for " + selectedColleague.name(),
                                text("Quickly capture an observation about this colleague.").dim())
                        .rounded(),
                spacer(1),
                categoryPanel,
                spacer(1),
                tagPanel,
                spacer(1),
                panel(
                                "Note",
                                textInput(noteInputState)
                                        .placeholder("Type your observation...")
                                        .focusable(false)
                                        .cursorRequiresFocus(false))
                        .rounded()
                        .id("content-panel")
                        .focusable()
                        .onKeyEvent(
                                ke -> {
                                    if (ke.isConfirm()) {
                                        handleNoteSubmit();
                                        return EventResult.HANDLED;
                                    }
                                    return handleTextInputKey(noteInputState, ke)
                                            ? EventResult.HANDLED
                                            : EventResult.UNHANDLED;
                                }),
                statusLine,
                text("  Tab to switch section  •  ↑↓ to navigate  •  Enter to toggle/save  •  Esc to go back  •  Ctrl+C to quit")
                        .dim());
    }
}
