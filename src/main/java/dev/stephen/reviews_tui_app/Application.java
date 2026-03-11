package dev.stephen.reviews_tui_app;

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.form;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;

import java.util.Arrays;
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
import dev.tamboui.widgets.form.FieldType;
import dev.tamboui.widgets.form.FormState;

@SpringBootApplication
public class Application extends ToolkitApp implements ApplicationRunner {

    @Autowired private ColleagueService colleagueService;
    @Autowired private NoteService noteService;

    private FormState formState;
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
        if (colleagues.isEmpty()) {
            colleagueService.createColleague("Steve");
            colleagues = colleagueService.listColleagues();
        }

        List<String> names = colleagues.stream().map(ColleagueDto::name).toList();

        formState =
                FormState.builder()
                        .selectField("colleague", names)
                        .selectField(
                                "category",
                                Arrays.stream(NoteCategory.values()).map(Enum::name).toList())
                        .selectField(
                                "tag", Arrays.stream(NoteTag.values()).map(Enum::name).toList())
                        .textField("content")
                        .build();

        runner().eventRouter()
                .addGlobalHandler(
                        event -> {
                            if (event instanceof KeyEvent ke && ke.isCtrlC()) {
                                quit();
                                return EventResult.HANDLED;
                            }
                            return EventResult.UNHANDLED;
                        });
    }

    private void handleSubmit(FormState fs) {
        runner().runOnRenderThread(
                        () -> {
                            int idx = fs.selectIndex("colleague");
                            String content = fs.textValue("content");

                            if (colleagues.isEmpty()) {
                                statusMessage =
                                        "No colleagues found. Add one via the REST API first.";
                                statusIsError = true;
                                return;
                            }
                            if (content.isBlank()) {
                                statusMessage = "Note content cannot be empty.";
                                statusIsError = true;
                                return;
                            }

                            ColleagueDto colleague = colleagues.get(idx);
                            NoteCategory category =
                                    NoteCategory.valueOf(fs.selectValue("category"));
                            NoteTag tag = NoteTag.valueOf(fs.selectValue("tag"));

                            try {
                                noteService.createNote(
                                        new ReviewNoteDto(content, category, tag, null, null, null),
                                        colleague);
                                statusMessage = "Note saved for " + colleague.name();
                                statusIsError = false;
                                fs.textField("content").clear();
                                fs.selectIndex("colleague", 0);
                                fs.selectIndex("category", 0);
                                fs.selectIndex("tag", 0);
                            } catch (Exception e) {
                                statusMessage = "Error: " + e.getMessage();
                                statusIsError = true;
                            }
                        });
    }

    @Override
    protected Element render() {
        if (formState == null) {
            return text("Loading...").dim();
        }

        Element statusLine =
                statusMessage.isBlank()
                        ? spacer(1)
                        : statusIsError
                                ? text("  " + statusMessage).red()
                                : text("  " + statusMessage).green();

        return column(
                panel(
                                "Colleague Performance Notes",
                                text("Quickly capture an observation about a colleague.").dim())
                        .rounded(),
                spacer(1),
                form(formState)
                        .field("colleague", "Colleague", FieldType.SELECT)
                        .field("category", "Category", FieldType.SELECT)
                        .field("tag", "Tag", FieldType.SELECT)
                        .field("content", "Note")
                        .labelWidth(12)
                        .submitOnEnter(true)
                        .onSubmit(this::handleSubmit)
                        .rounded(),
                statusLine,
                text("  Tab/arrow keys to navigate  •  Enter to save  •  Ctrl+C to quit").dim());
    }
}
