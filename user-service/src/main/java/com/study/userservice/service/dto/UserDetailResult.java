package com.study.userservice.service.dto;

import com.study.userservice.client.dto.OrderResponse;
import com.study.userservice.entity.User;

import java.util.List;

public record UserDetailResult(String userId, String email, String name, List<OrderResponse> orders) {

	public static UserDetailResult of(User user, List<OrderResponse> orders) {
		return new UserDetailResult(user.getUserId(), user.getEmail(), user.getName(), orders);
	}
}
