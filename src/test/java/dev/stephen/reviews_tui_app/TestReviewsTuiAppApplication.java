package dev.stephen.reviews_tui_app;

import org.springframework.boot.SpringApplication;

public class TestReviewsTuiAppApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReviewsTuiAppApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
