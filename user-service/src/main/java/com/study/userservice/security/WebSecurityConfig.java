package com.study.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	private static final String[] PUBLIC_PATHS = {
			"/actuator/**"
	};

	private final LoginUserDetailsService loginUserDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	public WebSecurityConfig(LoginUserDetailsService loginUserDetailsService, PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
		this.loginUserDetailsService = loginUserDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.objectMapper = objectMapper;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(loginUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder);

		return new ProviderManager(provider);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
			throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationManager(authenticationManager)
				.authorizeHttpRequests(authz -> authz
						.requestMatchers(PUBLIC_PATHS).permitAll()
						.requestMatchers(HttpMethod.POST, "/users").permitAll()
						.anyRequest().authenticated())
				.addFilter(authenticationFilter(authenticationManager))
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private AuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
		return new AuthenticationFilter(authenticationManager, jwtTokenProvider, objectMapper);
	}
}
