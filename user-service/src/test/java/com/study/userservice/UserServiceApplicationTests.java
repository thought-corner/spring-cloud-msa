package com.study.userservice;

import org.junit.jupiter.api.Test;
import com.study.userservice.support.MySqlTestContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"spring.config.import=", "spring.cloud.config.enabled=false"})
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
