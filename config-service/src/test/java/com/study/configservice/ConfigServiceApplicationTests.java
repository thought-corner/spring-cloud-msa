package com.study.configservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"native", "test"})
class ConfigServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
