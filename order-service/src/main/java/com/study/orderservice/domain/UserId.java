package com.study.orderservice.domain;

public record UserId(String value) {

	public UserId {
		if (value == null || value.isBlank()) {
			throw new OrderException(OrderErrorCode.BLANK_IDENTIFIER);
		}
	}

	public static UserId of(String value) {
		return new UserId(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
