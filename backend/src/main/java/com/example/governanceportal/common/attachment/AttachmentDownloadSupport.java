package com.example.governanceportal.common.attachment;

import com.example.governanceportal.common.error.BusinessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AttachmentDownloadSupport {

    private static final String FALLBACK_FILENAME = "download";

    public ResponseEntity<Resource> fromPath(Path filePath, String downloadFilename) {
        Path normalizedPath = filePath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedPath) || !Files.isReadable(normalizedPath)) {
            throw BusinessException.notFound("Attachment file was not found.");
        }

        try {
            return attachment(
                new FileSystemResource(normalizedPath),
                downloadFilename,
                probeMediaType(normalizedPath),
                Files.size(normalizedPath)
            );
        } catch (IOException error) {
            throw BusinessException.internal("Could not read attachment file.");
        }
    }

    public ResponseEntity<Resource> attachment(
        Resource resource,
        String downloadFilename,
        MediaType mediaType,
        long contentLength
    ) {
        HttpHeaders headers = headers(downloadFilename, mediaType);
        if (contentLength >= 0) {
            headers.setContentLength(contentLength);
        }

        return ResponseEntity.ok()
            .headers(headers)
            .body(resource);
    }

    public HttpHeaders headers(String downloadFilename, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType == null ? MediaType.APPLICATION_OCTET_STREAM : mediaType);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(safeFilename(downloadFilename), StandardCharsets.UTF_8)
            .build());
        headers.setCacheControl(CacheControl.noStore());
        headers.set("X-Content-Type-Options", "nosniff");
        return headers;
    }

    private MediaType probeMediaType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            return StringUtils.hasText(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception error) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String safeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return FALLBACK_FILENAME;
        }

        String normalized = filename.replace('\\', '/');
        String basename = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        String sanitized = basename.replaceAll("[\\p{Cntrl}\"]", "_");
        return StringUtils.hasText(sanitized) ? sanitized : FALLBACK_FILENAME;
    }
}
