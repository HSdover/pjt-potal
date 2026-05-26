package com.example.governanceportal.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import com.example.governanceportal.common.external.ExternalApiException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException error, HttpServletRequest request) {
        return build(error.getStatus(), error.getErrorCode(), error.getMessage(), request, List.of(), error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException error, HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = error.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> new ApiFieldError(fieldError.getField(), safeMessage(fieldError.getDefaultMessage())))
            .toList();

        return build(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_ERROR,
            "입력값을 확인하세요.",
            request,
            fieldErrors,
            error
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException error, HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = error.getConstraintViolations().stream()
            .map(violation -> new ApiFieldError(String.valueOf(violation.getPropertyPath()), safeMessage(violation.getMessage())))
            .toList();

        return build(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_ERROR,
            "입력값을 확인하세요.",
            request,
            fieldErrors,
            error
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException error, HttpServletRequest request) {
        return build(
            HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST,
            "요청 본문 형식이 올바르지 않습니다.",
            request,
            List.of(),
            error
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException error, HttpServletRequest request) {
        HttpStatus status = toHttpStatus(error.getStatusCode());
        return build(status, toErrorCode(status), reason(error), request, List.of(), error);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleErrorResponse(ErrorResponseException error, HttpServletRequest request) {
        HttpStatus status = toHttpStatus(error.getStatusCode());
        String message = error.getBody().getDetail() == null ? status.getReasonPhrase() : error.getBody().getDetail();
        return build(status, toErrorCode(status), message, request, List.of(), error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException error, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "접근 권한이 없습니다.", request, List.of(), error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException error, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, safeMessage(error.getMessage()), request, List.of(), error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccess(DataAccessException error, HttpServletRequest request) {
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.DATA_ACCESS_ERROR,
            "데이터 처리 중 오류가 발생했습니다.",
            request,
            List.of(),
            error
        );
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalApi(ExternalApiException error, HttpServletRequest request) {
        return build(error.getStatus(), error.getErrorCode(), error.getMessage(), request, List.of(), error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception error, HttpServletRequest request) {
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_ERROR,
            "서버 처리 중 오류가 발생했습니다.",
            request,
            List.of(),
            error
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
        HttpStatus status,
        ErrorCode code,
        String message,
        HttpServletRequest request,
        List<ApiFieldError> fieldErrors,
        Exception error
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
            status.value(),
            code,
            safeMessage(message),
            request.getRequestURI(),
            MDC.get(RequestIdFilter.MDC_KEY),
            fieldErrors
        );

        logError(status, response, error);
        return ResponseEntity.status(status).body(response);
    }

    private void logError(HttpStatus status, ApiErrorResponse response, Exception error) {
        if (status.is5xxServerError()) {
            log.error(
                "API error. status={}, code={}, path={}, requestId={}",
                response.status(),
                response.code(),
                response.path(),
                response.requestId(),
                error
            );
            return;
        }

        log.warn(
            "API rejected. status={}, code={}, path={}, requestId={}, message={}",
            response.status(),
            response.code(),
            response.path(),
            response.requestId(),
            response.message()
        );
    }

    private HttpStatus toHttpStatus(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
    }

    private ErrorCode toErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case CONFLICT -> ErrorCode.CONFLICT;
            default -> status.is5xxServerError() ? ErrorCode.INTERNAL_ERROR : ErrorCode.BAD_REQUEST;
        };
    }

    private String reason(ResponseStatusException error) {
        return error.getReason() == null ? error.getStatusCode().toString() : error.getReason();
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank() ? "요청 처리 중 오류가 발생했습니다." : message;
    }
}
