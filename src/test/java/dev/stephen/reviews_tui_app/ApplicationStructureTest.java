package dev.stephen.reviews_tui_app;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ApplicationStructureTest {

    @Test
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(ReviewsTuiAppApplication.class);
        modules.verify();
    }
}
