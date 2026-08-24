package com.study.orderservice.domain;

public record Quantity(int value) {

	public Quantity {
		if (value <= 0) {
			throw new OrderException(OrderErrorCode.INVALID_QUANTITY);
		}
	}

	public static Quantity of(int value) {
		return new Quantity(value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
