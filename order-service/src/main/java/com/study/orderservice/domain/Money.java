package com.study.orderservice.domain;

public record Money(long amount) {

	public Money {
		if (amount < 0) {
			throw new OrderException(OrderErrorCode.NEGATIVE_AMOUNT);
		}
	}

	public static Money of(long amount) {
		return new Money(amount);
	}

	public Money multiply(Quantity quantity) {
		try {
			return new Money(Math.multiplyExact(amount, (long) quantity.value()));
		} catch (ArithmeticException ex) {
			throw new OrderException(ex, OrderErrorCode.AMOUNT_OVERFLOW);
		}
	}

	public boolean isPositive() {
		return amount > 0;
	}

	@Override
	public String toString() {
		return String.valueOf(amount);
	}
}
