package com.example.governanceportal.reference.board.api;

import com.example.governanceportal.common.attachment.AttachmentDownloadSupport;
import com.example.governanceportal.common.attachment.AttachmentStorageService;
import com.example.governanceportal.common.attachment.AttachmentUploadResponse;
import com.example.governanceportal.common.attachment.StoredAttachment;
import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.reference.board.dto.RefBoardItem;
import com.example.governanceportal.reference.board.dto.RefBoardListRequest;
import com.example.governanceportal.reference.board.dto.RefBoardSaveRequest;
import com.example.governanceportal.reference.board.service.RefBoardService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reference/boards")
@PreAuthorize("@portalPermissionService.hasPermission(authentication, 'REF_VIEW')")
public class RefBoardController {

    private final RefBoardService refBoardService;
    private final AttachmentDownloadSupport attachmentDownloadSupport;
    private final AttachmentStorageService attachmentStorageService;

    public RefBoardController(
        RefBoardService refBoardService,
        AttachmentDownloadSupport attachmentDownloadSupport,
        AttachmentStorageService attachmentStorageService
    ) {
        this.refBoardService = refBoardService;
        this.attachmentDownloadSupport = attachmentDownloadSupport;
        this.attachmentStorageService = attachmentStorageService;
    }

    @PostMapping("/search")
    public ListResponse<RefBoardItem> search(@RequestBody RefBoardListRequest request) {
        return refBoardService.search(request);
    }

    @PostMapping(path = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentUploadResponse uploadAttachment(@RequestPart("file") MultipartFile file) {
        StoredAttachment stored = attachmentStorageService.store(file);
        return toAttachmentResponse(stored);
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String attachmentId) {
        StoredAttachment stored = attachmentStorageService.findById(attachmentId);
        return attachmentDownloadSupport.attachment(
            new FileSystemResource(stored.filePath()),
            stored.fileName(),
            mediaType(stored.contentType()),
            stored.size()
        );
    }

    @GetMapping("/{id}")
    public RefBoardItem findById(@PathVariable Long id) {
        return refBoardService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefBoardItem create(@Valid @RequestBody RefBoardSaveRequest request) {
        return refBoardService.create(request);
    }

    @PutMapping("/{id}")
    public RefBoardItem update(@PathVariable Long id, @Valid @RequestBody RefBoardSaveRequest request) {
        return refBoardService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        refBoardService.delete(id);
    }

    private AttachmentUploadResponse toAttachmentResponse(StoredAttachment stored) {
        return new AttachmentUploadResponse(
            stored.attachmentId(),
            stored.fileName(),
            stored.size(),
            stored.contentType(),
            "/api/reference/boards/attachments/" + stored.attachmentId()
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
