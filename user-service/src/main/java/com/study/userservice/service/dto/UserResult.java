package com.study.userservice.service.dto;

import com.study.userservice.entity.User;

public record UserResult(String userId, String email, String name) {

	public static UserResult from(User user) {
		return new UserResult(user.getUserId(), user.getEmail(), user.getName());
	}
}
