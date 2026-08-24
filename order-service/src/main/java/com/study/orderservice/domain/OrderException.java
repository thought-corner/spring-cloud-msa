package com.study.orderservice.domain;

public class OrderException extends RuntimeException {

	private final ErrorCode errorCode;

	public OrderException(ErrorCode errorCode) {
		super(errorCode.message());
		this.errorCode = errorCode;
	}

	public OrderException(Throwable cause, ErrorCode errorCode) {
		super(errorCode.message(), cause);
		this.errorCode = errorCode;
	}

	public ErrorCode errorCode() {
		return errorCode;
	}
}
