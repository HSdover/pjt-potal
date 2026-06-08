package com.example.governanceportal.common.attachment;

public record AttachmentUploadResponse(
    String attachmentId,
    String fileName,
    long size,
    String contentType,
    String downloadUrl
) {
}
