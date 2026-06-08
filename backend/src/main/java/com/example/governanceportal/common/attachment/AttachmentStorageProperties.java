package com.example.governanceportal.common.attachment;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AttachmentStorageProperties {

    private final String storageRoot;

    public AttachmentStorageProperties(@Value("${app.attachment.storage-root:}") String storageRoot) {
        this.storageRoot = storageRoot;
    }

    public Path storageRoot() {
        if (StringUtils.hasText(storageRoot)) {
            return Path.of(storageRoot).toAbsolutePath().normalize();
        }

        return Path.of(System.getProperty("java.io.tmpdir"), "governance-portal", "attachments")
            .toAbsolutePath()
            .normalize();
    }
}
