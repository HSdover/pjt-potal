package com.example.governanceportal.common.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.governanceportal.common.error.BusinessException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AttachmentDownloadSupportTest {

    private final AttachmentDownloadSupport support = new AttachmentDownloadSupport();

    @TempDir
    private Path tempDir;

    @Test
    void fromPathBuildsAttachmentResponseWithSafeFilename() throws Exception {
        Path filePath = tempDir.resolve("stored-file.txt");
        Files.writeString(filePath, "stored attachment", StandardCharsets.UTF_8);

        ResponseEntity<Resource> response = support.fromPath(filePath, "../unsafe/download.txt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentLength()).isEqualTo(Files.size(filePath));
        assertThat(response.getHeaders().getContentDisposition().toString())
            .contains("attachment")
            .contains("download.txt")
            .doesNotContain("unsafe");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeaders().getCacheControl()).contains("no-store");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getInputStream().readAllBytes())
            .isEqualTo("stored attachment".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void fromPathRejectsMissingFile() {
        Path missingPath = tempDir.resolve("missing.txt");

        assertThatThrownBy(() -> support.fromPath(missingPath, "missing.txt"))
            .isInstanceOfSatisfying(BusinessException.class, error ->
                assertThat(error.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
