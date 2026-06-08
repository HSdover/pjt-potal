package com.example.governanceportal.common.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.governanceportal.common.error.BusinessException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class AttachmentStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void storeWritesFileAndMetadataThenFindsItAgain() throws Exception {
        AttachmentStorageService service = new AttachmentStorageService(new AttachmentStorageProperties(tempDir.toString()));
        MultipartFile file = new MockMultipartFile(
            "file",
            "../report.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "stored by backend".getBytes(StandardCharsets.UTF_8)
        );

        StoredAttachment stored = service.store(file);
        StoredAttachment found = service.findById(stored.attachmentId());

        assertThat(stored.attachmentId()).isNotBlank();
        assertThat(found.fileName()).isEqualTo("report.txt");
        assertThat(found.contentType()).isEqualTo(MediaType.TEXT_PLAIN_VALUE);
        assertThat(found.size()).isEqualTo("stored by backend".getBytes(StandardCharsets.UTF_8).length);
        assertThat(Files.readString(found.filePath())).isEqualTo("stored by backend");
        assertThat(Files.exists(tempDir.resolve(stored.attachmentId() + ".properties"))).isTrue();
    }

    @Test
    void findByIdRejectsInvalidId() {
        AttachmentStorageService service = new AttachmentStorageService(new AttachmentStorageProperties(tempDir.toString()));

        assertThatThrownBy(() -> service.findById("../bad"))
            .isInstanceOf(BusinessException.class);
    }
}
