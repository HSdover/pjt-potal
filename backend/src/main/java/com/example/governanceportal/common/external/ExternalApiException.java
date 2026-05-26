package com.example.governanceportal.common.external;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class ExternalApiException extends BusinessException {

    private final String externalSystem;
    private final String api;
    private final Integer externalStatus;
    private final String requestId;

    public ExternalApiException(
        String externalSystem,
        String api,
        Integer externalStatus,
        String requestId,
        String message
    ) {
        super(HttpStatus.BAD_GATEWAY, ErrorCode.EXTERNAL_API_ERROR, message);
        this.externalSystem = externalSystem;
        this.api = api;
        this.externalStatus = externalStatus;
        this.requestId = requestId;
    }

    public ExternalApiException(
        String externalSystem,
        String api,
        Integer externalStatus,
        String requestId,
        String message,
        Throwable cause
    ) {
        super(HttpStatus.BAD_GATEWAY, ErrorCode.EXTERNAL_API_ERROR, message, cause);
        this.externalSystem = externalSystem;
        this.api = api;
        this.externalStatus = externalStatus;
        this.requestId = requestId;
    }

    public String getExternalSystem() {
        return externalSystem;
    }

    public String getApi() {
        return api;
    }

    public Integer getExternalStatus() {
        return externalStatus;
    }

    public String getRequestId() {
        return requestId;
    }

    public static ExternalApiException httpFailure(String externalSystem, String api, int status, String requestId) {
        return new ExternalApiException(
            externalSystem,
            api,
            status,
            requestId,
            "외부 시스템 호출에 실패했습니다. externalSystem=%s, status=%d".formatted(externalSystem, status)
        );
    }

    public static ExternalApiException connectionFailure(
        String externalSystem,
        String api,
        String requestId,
        Throwable cause
    ) {
        return new ExternalApiException(
            externalSystem,
            api,
            null,
            requestId,
            "외부 시스템 통신 중 오류가 발생했습니다. externalSystem=%s".formatted(externalSystem),
            cause
        );
    }
}
