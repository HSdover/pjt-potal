package com.example.governanceportal.common.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String code,
    String message,
    String path,
    String requestId,
    List<ApiFieldError> fieldErrors
) {
    public static ApiErrorResponse of(
        int status,
        ErrorCode code,
        String message,
        String path,
        String requestId,
        List<ApiFieldError> fieldErrors
    ) {
        return new ApiErrorResponse(
            OffsetDateTime.now(),
            status,
            code.name(),
            message,
            path,
            requestId,
            fieldErrors == null ? List.of() : List.copyOf(fieldErrors)
        );
    }
}
