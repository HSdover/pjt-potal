package com.example.governanceportal.common.external;

import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

final class ExternalApiLogSanitizer {

    private ExternalApiLogSanitizer() {
    }

    static String externalSystem(HttpHeaders headers, URI uri) {
        String marker = headers.getFirst(ExternalApiHeaders.EXTERNAL_SYSTEM);
        if (StringUtils.hasText(marker)) {
            return normalize(marker);
        }

        return normalize(uri.getHost() == null ? "UNKNOWN" : uri.getHost());
    }

    static String api(String method, URI uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme() + "://";
        String host = uri.getHost() == null ? "" : uri.getHost();
        int port = uri.getPort();
        String portText = port < 0 ? "" : ":" + port;
        String path = StringUtils.hasText(uri.getPath()) ? uri.getPath() : "/";
        return method + " " + scheme + host + portText + path;
    }

    static String reason(Throwable error) {
        String message = error.getMessage();
        if (!StringUtils.hasText(message)) {
            return error.getClass().getSimpleName();
        }

        return message
            .replaceAll("(?i)(password|token|authorization|cookie|samlresponse|privatekey|wallet)=([^\\s&]+)", "$1=***")
            .replaceAll("[\\r\\n]+", " ");
    }

    private static String normalize(String value) {
        return value.trim()
            .replaceAll("[^A-Za-z0-9_.-]", "_")
            .replaceAll("_+", "_");
    }
}
