package com.study.orderservice;

import org.junit.jupiter.api.Test;
import com.study.orderservice.support.MySqlTestContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
