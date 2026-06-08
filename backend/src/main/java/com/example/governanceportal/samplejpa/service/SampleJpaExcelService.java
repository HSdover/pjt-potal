package com.example.governanceportal.samplejpa.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.common.excel.ExcelColumnSpec;
import com.example.governanceportal.common.excel.ExcelExportSupport;
import com.example.governanceportal.common.excel.ExcelFileValidator;
import com.example.governanceportal.common.excel.ExcelImportError;
import com.example.governanceportal.common.excel.ExcelImportResult;
import com.example.governanceportal.samplejpa.domain.SampleJpa;
import com.example.governanceportal.samplejpa.dto.SampleJpaExcelLargeExportResponse;
import com.example.governanceportal.samplejpa.dto.SampleJpaExcelRow;
import com.example.governanceportal.samplejpa.dto.SampleJpaItem;
import com.example.governanceportal.samplejpa.dto.SampleJpaListRequest;
import com.example.governanceportal.samplejpa.repository.SampleJpaRepository;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SampleJpaExcelService {

    public static final int STANDARD_EXPORT_LIMIT = 50_000;
    private static final int XLSX_SHEET_ROW_LIMIT = 1_048_576;
    private static final int EXPORT_CHUNK_SIZE = 1_000;
    private static final int IMPORT_ROW_LIMIT = 10_000;

    private final SampleJpaRepository sampleJpaRepository;
    private final ExcelExportSupport excelExportSupport;
    private final ExcelFileValidator excelFileValidator;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Map<String, SampleJpaExcelLargeExportJob> largeExportJobs = new ConcurrentHashMap<>();
    private final Path largeExportDirectory;

    public SampleJpaExcelService(
        SampleJpaRepository sampleJpaRepository,
        ExcelExportSupport excelExportSupport,
        ExcelFileValidator excelFileValidator
    ) {
        this.sampleJpaRepository = sampleJpaRepository;
        this.excelExportSupport = excelExportSupport;
        this.excelFileValidator = excelFileValidator;
        this.largeExportDirectory = Path.of(System.getProperty("java.io.tmpdir"), "governance-portal", "sample-jpa-excel");
    }

    @Transactional(readOnly = true)
    public long validateStandardExport(SampleJpaListRequest request) {
        long totalRows = sampleJpaRepository.countBySearchFilter(request.filters());
        if (totalRows > STANDARD_EXPORT_LIMIT) {
            throw BusinessException.badRequest("Standard Excel export supports up to 50,000 rows. Use large Excel export.");
        }
        return totalRows;
    }

    @Transactional(readOnly = true)
    public void writeExport(SampleJpaListRequest request, OutputStream outputStream) {
        writeExportInternal(request, outputStream);
    }

    @Transactional(readOnly = true)
    public SampleJpaExcelLargeExportResponse requestLargeExport(SampleJpaListRequest request) {
        long totalRows = sampleJpaRepository.countBySearchFilter(request.filters());
        if (totalRows > XLSX_SHEET_ROW_LIMIT - 1L) {
            throw BusinessException.badRequest("Excel export exceeds the .xlsx sheet row limit.");
        }

        String jobId = UUID.randomUUID().toString();
        SampleJpaExcelLargeExportJob job = new SampleJpaExcelLargeExportJob(jobId, totalRows);
        largeExportJobs.put(jobId, job);

        executorService.submit(() -> runLargeExport(job, request));

        return toResponse(job);
    }

    public SampleJpaExcelLargeExportResponse getLargeExport(String jobId) {
        return toResponse(findJob(jobId));
    }

    public Path completedLargeExportFile(String jobId) {
        SampleJpaExcelLargeExportJob job = findJob(jobId);
        if (job.getStatus() != SampleJpaExcelLargeExportStatus.COMPLETED || job.getFilePath() == null) {
            throw BusinessException.badRequest("Large Excel export is not completed.");
        }
        if (!Files.exists(job.getFilePath())) {
            throw BusinessException.notFound("Large Excel export file was not found.");
        }
        return job.getFilePath();
    }

    @Transactional
    public ExcelImportResult importExcel(MultipartFile file) {
        excelFileValidator.validateXlsx(file);

        List<ImportedRow> rows = readRows(file);
        List<ExcelImportError> errors = validateRows(rows);
        if (!errors.isEmpty()) {
            return ExcelImportResult.failed(rows.size(), errors);
        }

        List<SampleJpa> samples = rows.stream()
            .map(this::toEntity)
            .toList();

        sampleJpaRepository.saveAllAndFlush(samples);
        return ExcelImportResult.success(rows.size());
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdown();
    }

    private void runLargeExport(SampleJpaExcelLargeExportJob job, SampleJpaListRequest request) {
        job.running();
        try {
            Files.createDirectories(largeExportDirectory);
            Path filePath = largeExportDirectory.resolve("sample-jpa-large-export-" + job.getJobId() + ".xlsx");
            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                writeExportInternal(request, outputStream);
            }
            job.completed(filePath);
        } catch (Exception error) {
            job.failed("Large Excel export failed.");
        }
    }

    private void writeExportInternal(SampleJpaListRequest request, OutputStream outputStream) {
        excelExportSupport.write(outputStream, "Sample JPA", exportColumns(), chunkWriter -> {
            int pageNo = 0;
            while (true) {
                Pageable pageable = PageRequest.of(pageNo, EXPORT_CHUNK_SIZE, SampleJpaSorts.toSort(request.sort()));
                List<SampleJpaItem> rows = sampleJpaRepository.searchRows(request.filters(), pageable).stream()
                    .map(SampleJpaItem::from)
                    .toList();

                if (rows.isEmpty()) {
                    return;
                }

                chunkWriter.write(rows);

                if (rows.size() < EXPORT_CHUNK_SIZE) {
                    return;
                }
                pageNo++;
            }
        });
    }

    private List<ExcelColumnSpec<SampleJpaItem>> exportColumns() {
        return List.of(
            new ExcelColumnSpec<>("ID", SampleJpaItem::id),
            new ExcelColumnSpec<>("Name", SampleJpaItem::name),
            new ExcelColumnSpec<>("Description", SampleJpaItem::description)
        );
    }

    private List<ImportedRow> readRows(MultipartFile file) {
        SampleJpaExcelRowListener listener = new SampleJpaExcelRowListener();
        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, SampleJpaExcelRow.class, listener).sheet().doRead();
        } catch (IOException error) {
            throw BusinessException.badRequest("Could not read Excel upload file.");
        }
        return listener.rows();
    }

    private List<ExcelImportError> validateRows(List<ImportedRow> rows) {
        List<ExcelImportError> errors = new ArrayList<>();
        Set<Long> seenIds = new LinkedHashSet<>();

        if (rows.size() > IMPORT_ROW_LIMIT) {
            errors.add(new ExcelImportError(0, "file", "Synchronous Excel import supports up to 10,000 rows."));
            return errors;
        }

        for (ImportedRow importedRow : rows) {
            SampleJpaExcelRow row = importedRow.row();
            int rowIndex = importedRow.rowIndex();

            if (row.getId() != null) {
                if (row.getId() <= 0) {
                    errors.add(new ExcelImportError(rowIndex, "ID", "ID must be greater than zero."));
                } else if (!seenIds.add(row.getId())) {
                    errors.add(new ExcelImportError(rowIndex, "ID", "Duplicate ID in upload file."));
                } else if (!sampleJpaRepository.existsById(row.getId())) {
                    errors.add(new ExcelImportError(rowIndex, "ID", "ID does not exist."));
                }
            }

            String name = normalize(row.getName());
            if (!StringUtils.hasText(name)) {
                errors.add(new ExcelImportError(rowIndex, "Name", "Name is required."));
            } else if (name.length() > 200) {
                errors.add(new ExcelImportError(rowIndex, "Name", "Name must be 200 characters or less."));
            } else if (isFormulaLike(name)) {
                errors.add(new ExcelImportError(rowIndex, "Name", "Formula-like values are not allowed."));
            }

            String description = normalize(row.getDescription());
            if (description != null && description.length() > 1000) {
                errors.add(new ExcelImportError(rowIndex, "Description", "Description must be 1,000 characters or less."));
            } else if (isFormulaLike(description)) {
                errors.add(new ExcelImportError(rowIndex, "Description", "Formula-like values are not allowed."));
            }
        }

        return errors;
    }

    private SampleJpa toEntity(ImportedRow importedRow) {
        SampleJpaExcelRow row = importedRow.row();
        String name = normalize(row.getName());
        String description = normalize(row.getDescription());

        if (row.getId() == null) {
            return new SampleJpa(name, description);
        }

        SampleJpa sample = sampleJpaRepository.findById(row.getId())
            .orElseThrow(() -> BusinessException.notFound("Sample JPA row was not found."));
        sample.update(name, description);
        return sample;
    }

    private SampleJpaExcelLargeExportJob findJob(String jobId) {
        SampleJpaExcelLargeExportJob job = largeExportJobs.get(jobId);
        if (job == null) {
            throw BusinessException.notFound("Large Excel export job was not found.");
        }
        return job;
    }

    private SampleJpaExcelLargeExportResponse toResponse(SampleJpaExcelLargeExportJob job) {
        String downloadUrl = job.getStatus() == SampleJpaExcelLargeExportStatus.COMPLETED
            ? "/api/samples-jpa/excel/large-download/" + job.getJobId() + "/file"
            : null;

        return new SampleJpaExcelLargeExportResponse(
            job.getJobId(),
            job.getStatus().name(),
            job.getTotalRows(),
            job.getMessage(),
            downloadUrl
        );
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private boolean isFormulaLike(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' || first == '\t';
    }

    private record ImportedRow(
        int rowIndex,
        SampleJpaExcelRow row
    ) {
    }

    private static class SampleJpaExcelRowListener extends AnalysisEventListener<SampleJpaExcelRow> {

        private final List<ImportedRow> rows = new ArrayList<>();

        @Override
        public void invoke(SampleJpaExcelRow row, AnalysisContext context) {
            if (row != null && !row.isBlank()) {
                rows.add(new ImportedRow(context.readRowHolder().getRowIndex() + 1, row));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }

        List<ImportedRow> rows() {
            return rows;
        }
    }
}
