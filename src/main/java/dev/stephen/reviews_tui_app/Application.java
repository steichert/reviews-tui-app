package dev.stephen.reviews_tui_app;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;

@SpringBootApplication
public class Application extends ToolkitApp {

	public static void main(String[] args) throws Exception {
		var app = new Application();
		app.run();
	}

	@Override
	protected Element render() {
		return panel("Hello",
				text("Welcome to TamboUI DSL!").bold().cyan(),
				spacer(),
				text("Press 'q' to quit").dim()
		).rounded();
	}
}
