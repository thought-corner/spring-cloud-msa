package com.study.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.userservice.controller.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final String TOKEN_HEADER = "token";
	private static final String USER_ID_HEADER = "userId";

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	public AuthenticationFilter(AuthenticationManager authenticationManager,
			JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
		super(authenticationManager);
		this.jwtTokenProvider = jwtTokenProvider;
		this.objectMapper = objectMapper;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			LoginRequest credentials = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

			return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
					credentials.email(), credentials.password(), new ArrayList<>()));
		} catch (IOException ex) {
			throw new UncheckedIOException("Failed to read login request body", ex);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain, Authentication authentication) {
		String userId = ((LoginUser) authentication.getPrincipal()).userId();

		response.addHeader(TOKEN_HEADER, jwtTokenProvider.createToken(userId));
		response.addHeader(USER_ID_HEADER, userId);
	}
}
