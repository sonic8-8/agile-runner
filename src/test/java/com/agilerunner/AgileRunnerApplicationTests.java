package com.agilerunner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "agile-runner.agent-runtime.enabled=false")
class AgileRunnerApplicationTests {

	@Test
	void contextLoads() {
	}

}
