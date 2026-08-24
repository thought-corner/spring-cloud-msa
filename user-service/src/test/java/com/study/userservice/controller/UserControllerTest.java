package com.study.userservice.controller;

import com.study.userservice.security.JwtTokenProvider;
import com.study.userservice.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.config.import=", "spring.cloud.config.enabled=false"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void 토큰_없이_사용자_조회에_접근하면_거부된다() throws Exception {
		mockMvc.perform(get("/users/{userId}", "no-such-user"))
				.andExpect(status().isForbidden());
	}

	@Test
	void 위조된_토큰으로_접근하면_거부된다() throws Exception {
		mockMvc.perform(get("/users/{userId}", "no-such-user")
						.header("Authorization", "Bearer not-a-real-token"))
				.andExpect(status().isForbidden());
	}

	@Test
	void 유효한_토큰으로_존재하지_않는_사용자를_조회하면_404를_반환한다() throws Exception {
		String token = jwtTokenProvider.createToken("tester");

		mockMvc.perform(get("/users/{userId}", "no-such-user")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}
}
