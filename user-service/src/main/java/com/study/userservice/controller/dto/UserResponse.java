package com.study.userservice.controller.dto;

import com.study.userservice.service.dto.UserResult;

public record UserResponse(String email, String name, String userId) {

	public static UserResponse from(UserResult result) {
		return new UserResponse(result.email(), result.name(), result.userId());
	}
}
