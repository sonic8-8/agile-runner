package com.agilerunner;

import org.springframework.boot.SpringApplication;

public class TestAgileRunnerApplication {

	public static void main(String[] args) {
		SpringApplication.from(AgileRunnerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
