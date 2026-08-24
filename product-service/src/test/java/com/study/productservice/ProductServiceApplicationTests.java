package com.study.productservice;

import org.junit.jupiter.api.Test;
import com.study.productservice.support.MySqlTestContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
class ProductServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
