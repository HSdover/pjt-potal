package com.example.governanceportal.reference.api;

import com.example.governanceportal.common.attachment.AttachmentDownloadSupport;
import com.example.governanceportal.common.attachment.AttachmentStorageService;
import com.example.governanceportal.common.attachment.AttachmentUploadResponse;
import com.example.governanceportal.common.attachment.StoredAttachment;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/_ref/attachments")
@PreAuthorize("@portalPermissionService.hasPermission(authentication, 'REF_VIEW')")
public class ReferenceAttachmentController {

    private static final byte[] SAMPLE_ATTACHMENT = """
        Governance Portal attachment download sample.
        This file is served by the backend common attachment download module.
        """.getBytes(StandardCharsets.UTF_8);

    private final AttachmentDownloadSupport attachmentDownloadSupport;
    private final AttachmentStorageService attachmentStorageService;

    public ReferenceAttachmentController(
        AttachmentDownloadSupport attachmentDownloadSupport,
        AttachmentStorageService attachmentStorageService
    ) {
        this.attachmentDownloadSupport = attachmentDownloadSupport;
        this.attachmentStorageService = attachmentStorageService;
    }

    @GetMapping("/sample")
    public ResponseEntity<Resource> downloadSampleAttachment() {
        return attachmentDownloadSupport.attachment(
            new ByteArrayResource(SAMPLE_ATTACHMENT),
            "attachment-sample.txt",
            MediaType.TEXT_PLAIN,
            SAMPLE_ATTACHMENT.length
        );
    }

    @PostMapping("/sample")
    public AttachmentUploadResponse uploadSampleAttachment(@RequestPart("file") MultipartFile file) {
        StoredAttachment stored = attachmentStorageService.store(file);
        return toResponse(stored);
    }

    @GetMapping("/sample/{attachmentId}")
    public ResponseEntity<Resource> downloadStoredSampleAttachment(@PathVariable String attachmentId) {
        StoredAttachment stored = attachmentStorageService.findById(attachmentId);
        return attachmentDownloadSupport.attachment(
            new FileSystemResource(stored.filePath()),
            stored.fileName(),
            mediaType(stored.contentType()),
            stored.size()
        );
    }

    private AttachmentUploadResponse toResponse(StoredAttachment stored) {
        return new AttachmentUploadResponse(
            stored.attachmentId(),
            stored.fileName(),
            stored.size(),
            stored.contentType(),
            "/api/_ref/attachments/sample/" + stored.attachmentId()
        );
    }

    private MediaType mediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception error) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
