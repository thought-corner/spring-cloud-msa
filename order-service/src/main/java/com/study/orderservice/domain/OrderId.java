package com.study.orderservice.domain;

import java.util.UUID;

public record OrderId(String value) {

	public OrderId {
		if (value == null || value.isBlank()) {
			throw new OrderException(OrderErrorCode.BLANK_IDENTIFIER);
		}
	}

	public static OrderId of(String value) {
		return new OrderId(value);
	}

	public static OrderId generate() {
		return new OrderId(UUID.randomUUID().toString());
	}

	@Override
	public String toString() {
		return value;
	}
}
