package dev.stephen.reviews_tui_app;

import static dev.tamboui.toolkit.Toolkit.formField;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.widgets.form.SelectFieldState;
import dev.tamboui.widgets.input.TextInputState;

@SpringBootApplication
public class Application extends ToolkitApp {

  private final TextInputState colleagueNameState = new TextInputState();
  private final SelectFieldState categoryState =
      new SelectFieldState("TECHNICAL_ABILITY", "RESPONSIBILITY_TO_OTHERS", "CUSTOMER_SUCCESS", "GENERAL");
  private final SelectFieldState tagState =
      new SelectFieldState("HIGHLIGHT", "IMPROVEMENT", "NONE");

  public static void main(String[] args) throws Exception {
    var app = new Application();
    app.run();
  }

  @Override
  protected Element render() {
    return panel(
            "Add Note",
            text("Colleague Notes CLI").bold().cyan(),
            spacer(),
            formField("Colleague", colleagueNameState),
            formField("Category", categoryState),
            formField("Tag", tagState),
            spacer(),
            text("Tab: next field  |  Enter: submit  |  q: quit").dim())
        .rounded();
  }
}
