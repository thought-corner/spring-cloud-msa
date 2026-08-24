package com.study.userservice.controller.dto;

import com.study.userservice.client.dto.OrderResponse;
import com.study.userservice.service.dto.UserDetailResult;

import java.util.List;

public record UserDetailResponse(String email, String name, String userId, List<OrderResponse> orders) {

	public static UserDetailResponse from(UserDetailResult result) {
		return new UserDetailResponse(result.email(), result.name(), result.userId(), result.orders());
	}
}
