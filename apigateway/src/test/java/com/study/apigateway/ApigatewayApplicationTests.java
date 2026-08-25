package com.study.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"spring.config.import=", "spring.cloud.config.enabled=false"})
@ActiveProfiles("test")
class ApigatewayApplicationTests {

	@Test
	void contextLoads() {
	}
}
