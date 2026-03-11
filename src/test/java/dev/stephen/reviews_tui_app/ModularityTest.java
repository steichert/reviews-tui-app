package dev.stephen.reviews_tui_app;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTest {

    @Test
    void applicationModules() {
        final ApplicationModules modules = ApplicationModules.of(Application.class);
        modules.verify();
    }

    @Test
    void createDocumentation() {
        final ApplicationModules modules = ApplicationModules.of(Application.class);
        new Documenter(modules).writeDocumentation();
    }
}
