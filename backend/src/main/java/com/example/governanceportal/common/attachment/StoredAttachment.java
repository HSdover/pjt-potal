package com.example.governanceportal.common.attachment;

import java.nio.file.Path;

public record StoredAttachment(
    String attachmentId,
    String fileName,
    String contentType,
    long size,
    Path filePath
) {
}
