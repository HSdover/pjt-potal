package com.example.governanceportal.common.logging;

import com.example.governanceportal.common.error.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ApplicationLoggingAspect {

    private static final Logger apiLog = LoggerFactory.getLogger("api");
    private static final Logger serviceLog = LoggerFactory.getLogger("service");

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            apiLog.info(
                "API handled. method={}, path={}, handler={}, elapsedMs={}",
                requestMethod(),
                requestPath(),
                signature(joinPoint),
                elapsedMs(startedAt)
            );
            return result;
        } catch (Throwable error) {
            if (isExpected(error)) {
                apiLog.debug(
                    "API rejected before handler completion. method={}, path={}, handler={}, elapsedMs={}, error={}",
                    requestMethod(),
                    requestPath(),
                    signature(joinPoint),
                    elapsedMs(startedAt),
                    error.getClass().getSimpleName()
                );
            } else {
                apiLog.warn(
                    "API failed before handler completion. method={}, path={}, handler={}, elapsedMs={}, error={}",
                    requestMethod(),
                    requestPath(),
                    signature(joinPoint),
                    elapsedMs(startedAt),
                    error.getClass().getSimpleName()
                );
            }
            throw error;
        }
    }

    @Around("within(com.example.governanceportal..service..*)")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            serviceLog.debug("Service handled. method={}, elapsedMs={}", signature(joinPoint), elapsedMs(startedAt));
            return result;
        } catch (Throwable error) {
            if (isExpected(error)) {
                serviceLog.debug(
                    "Service rejected. method={}, elapsedMs={}, error={}",
                    signature(joinPoint),
                    elapsedMs(startedAt),
                    error.getClass().getSimpleName()
                );
            } else {
                serviceLog.warn(
                    "Service failed. method={}, elapsedMs={}, error={}",
                    signature(joinPoint),
                    elapsedMs(startedAt),
                    error.getClass().getSimpleName()
                );
            }
            throw error;
        }
    }

    private boolean isExpected(Throwable error) {
        return error instanceof BusinessException || error instanceof IllegalArgumentException;
    }

    private String signature(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String requestMethod() {
        HttpServletRequest request = currentRequest();
        return request == null ? "-" : request.getMethod();
    }

    private String requestPath() {
        HttpServletRequest request = currentRequest();
        return request == null ? "-" : request.getRequestURI();
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
