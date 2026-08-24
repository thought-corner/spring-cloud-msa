package com.study.orderservice.domain;

public record ProductId(String value) {

	public ProductId {
		if (value == null || value.isBlank()) {
			throw new OrderException(OrderErrorCode.BLANK_IDENTIFIER);
		}
	}

	public static ProductId of(String value) {
		return new ProductId(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
