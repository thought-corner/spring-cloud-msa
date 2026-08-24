package com.study.orderservice.domain;

import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {

    REQUEST_VALIDATION("ORDER-4000", HttpStatus.BAD_REQUEST, "request validation failed"),
    INVALID_QUANTITY("ORDER-4001", HttpStatus.BAD_REQUEST, "quantity must be greater than zero"),
    NEGATIVE_AMOUNT("ORDER-4002", HttpStatus.BAD_REQUEST, "money must not be negative"),
    AMOUNT_OVERFLOW("ORDER-4003", HttpStatus.BAD_REQUEST, "amount is too large"),
    BLANK_IDENTIFIER("ORDER-4004", HttpStatus.BAD_REQUEST, "identifier must not be blank"),
    NON_POSITIVE_UNIT_PRICE("ORDER-4005", HttpStatus.BAD_REQUEST, "unit price must be greater than zero");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    OrderErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }
}
