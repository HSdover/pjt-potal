package com.example.governanceportal.samplejpa.api;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.excel.ExcelImportResult;
import com.example.governanceportal.common.excel.ExcelResponseHeaders;
import com.example.governanceportal.samplejpa.dto.SampleJpaCreateRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaExcelLargeExportResponse;
import com.example.governanceportal.samplejpa.dto.SampleJpaItem;
import com.example.governanceportal.samplejpa.dto.SampleJpaListRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaUpdateRequest;
import com.example.governanceportal.samplejpa.service.SampleJpaExcelService;
import com.example.governanceportal.samplejpa.service.SampleJpaService;
import java.nio.file.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/samples-jpa")
public class SampleJpaController {

    private final SampleJpaService sampleJpaService;
    private final SampleJpaExcelService sampleJpaExcelService;

    public SampleJpaController(SampleJpaService sampleJpaService, SampleJpaExcelService sampleJpaExcelService) {
        this.sampleJpaService = sampleJpaService;
        this.sampleJpaExcelService = sampleJpaExcelService;
    }

    @PostMapping("/search")
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_READ')")
    public ListResponse<SampleJpaItem> search(@RequestBody SampleJpaListRequest request) {
        return sampleJpaService.search(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_CREATE')")
    public SampleJpaItem create(@RequestBody SampleJpaCreateRequest request) {
        return sampleJpaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_UPDATE')")
    public SampleJpaItem update(@PathVariable Long id, @RequestBody SampleJpaUpdateRequest request) {
        return sampleJpaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_DELETE')")
    public void delete(@PathVariable Long id) {
        sampleJpaService.delete(id);
    }

    @PostMapping("/excel/download")
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_EXPORT')")
    public ResponseEntity<StreamingResponseBody> downloadExcel(@RequestBody SampleJpaListRequest request) {
        sampleJpaExcelService.validateStandardExport(request);
        StreamingResponseBody body = outputStream -> sampleJpaExcelService.writeExport(request, outputStream);

        return ResponseEntity.ok()
            .headers(ExcelResponseHeaders.attachment("sample-jpa.xlsx"))
            .body(body);
    }

    @PostMapping("/excel/large-download")
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_EXPORT')")
    public SampleJpaExcelLargeExportResponse requestLargeDownload(@RequestBody SampleJpaListRequest request) {
        return sampleJpaExcelService.requestLargeExport(request);
    }

    @GetMapping("/excel/large-download/{jobId}")
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_EXPORT')")
    public SampleJpaExcelLargeExportResponse getLargeDownload(@PathVariable String jobId) {
        return sampleJpaExcelService.getLargeExport(jobId);
    }

    @GetMapping("/excel/large-download/{jobId}/file")
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_EXPORT')")
    public ResponseEntity<Resource> downloadLargeExcel(@PathVariable String jobId) {
        Path filePath = sampleJpaExcelService.completedLargeExportFile(jobId);
        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
            .headers(ExcelResponseHeaders.attachment("sample-jpa-large-" + jobId + ".xlsx"))
            .body(resource);
    }

    @PostMapping("/excel/upload")
    @PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_IMPORT')")
    public ExcelImportResult uploadExcel(@RequestPart("file") MultipartFile file) {
        return sampleJpaExcelService.importExcel(file);
    }
}
