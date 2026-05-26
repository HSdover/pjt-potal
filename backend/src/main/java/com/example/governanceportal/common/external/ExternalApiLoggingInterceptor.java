package com.example.governanceportal.common.external;

import com.example.governanceportal.common.error.RequestIdFilter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class ExternalApiLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger("external-api");

    private final ExternalApiProperties properties;

    public ExternalApiLoggingInterceptor(ExternalApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution
    ) throws IOException {
        String externalSystem = ExternalApiLogSanitizer.externalSystem(request.getHeaders(), request.getURI());
        String api = ExternalApiLogSanitizer.api(request.getMethod().name(), request.getURI());
        String requestId = requestId();

        request.getHeaders().remove(ExternalApiHeaders.EXTERNAL_SYSTEM);
        if (!request.getHeaders().containsKey(RequestIdFilter.HEADER_NAME) && !"-".equals(requestId)) {
            request.getHeaders().set(RequestIdFilter.HEADER_NAME, requestId);
        }

        long startedAt = System.nanoTime();
        try {
            ClientHttpResponse response = execution.execute(request, body);
            int status = response.getStatusCode().value();
            if (properties.isLoggingEnabled()) {
                if (response.getStatusCode().isError()) {
                    log.warn(
                        "External API rejected. externalSystem={}, api={}, status={}, elapsedMs={}, requestId={}",
                        externalSystem,
                        api,
                        status,
                        elapsedMs(startedAt),
                        requestId
                    );
                } else {
                    log.info(
                        "External API handled. externalSystem={}, api={}, status={}, elapsedMs={}, requestId={}",
                        externalSystem,
                        api,
                        status,
                        elapsedMs(startedAt),
                        requestId
                    );
                }
            }

            if (response.getStatusCode().isError()) {
                response.close();
                throw ExternalApiException.httpFailure(externalSystem, api, status, requestId);
            }
            return response;
        } catch (IOException error) {
            if (properties.isLoggingEnabled()) {
                log.warn(
                    "External API failed. externalSystem={}, api={}, elapsedMs={}, requestId={}, reason={}",
                    externalSystem,
                    api,
                    elapsedMs(startedAt),
                    requestId,
                    ExternalApiLogSanitizer.reason(error)
                );
            }
            throw ExternalApiException.connectionFailure(externalSystem, api, requestId, error);
        } catch (RuntimeException error) {
            if (error instanceof ExternalApiException) {
                throw error;
            }

            if (properties.isLoggingEnabled()) {
                log.warn(
                    "External API failed. externalSystem={}, api={}, elapsedMs={}, requestId={}, reason={}",
                    externalSystem,
                    api,
                    elapsedMs(startedAt),
                    requestId,
                    ExternalApiLogSanitizer.reason(error)
                );
            }
            throw ExternalApiException.connectionFailure(externalSystem, api, requestId, error);
        }
    }

    private String requestId() {
        String value = MDC.get(RequestIdFilter.MDC_KEY);
        return value == null || value.isBlank() ? "-" : value;
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
