package com.study.orderservice.domain;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	String name();

	String code();

	String message();

	HttpStatus httpStatus();
}
