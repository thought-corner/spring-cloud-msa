package com.study.orderservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class OwnDataAccessAspect {

    public static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";

    @Before("@annotation(ownDataOnly)")
    public void enforce(JoinPoint joinPoint, OwnDataOnly ownDataOnly) {
        String ownerId = resolveOwnerId(joinPoint, ownDataOnly.value());
        String authenticatedUser = currentRequest().getHeader(AUTHENTICATED_USER_HEADER);

        if (authenticatedUser == null || authenticatedUser.isBlank() || !authenticatedUser.equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 소유의 자원만 접근할 수 있습니다.");
        }
    }

    private String resolveOwnerId(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterName.equals(parameterNames[i])) {
                return args[i] == null ? null : args[i].toString();
            }
        }
        throw new IllegalStateException("@OwnDataOnly(\"" + parameterName + "\") parameter not found on " + signature.getMethod());
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No current HTTP request available for @OwnDataOnly");
        }
        return attributes.getRequest();
    }
}
