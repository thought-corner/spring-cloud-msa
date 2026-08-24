package com.study.orderservice.presentation;

import com.study.orderservice.domain.OrderException;
import com.study.orderservice.domain.ErrorCode;
import com.study.orderservice.domain.OrderErrorCode;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class OrderExceptionHandler {

    private static final String CODE_PROPERTY = "code";

    @ExceptionHandler(OrderException.class)
    public ProblemDetail handleDomainRuleViolation(OrderException exception) {
        return problemDetail(exception.errorCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleRequestValidationFailure(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return problemDetail(OrderErrorCode.REQUEST_VALIDATION, detail);
    }

    private ProblemDetail problemDetail(ErrorCode errorCode, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.httpStatus(), detail);
        problemDetail.setProperty(CODE_PROPERTY, errorCode.code());
        return problemDetail;
    }
}
