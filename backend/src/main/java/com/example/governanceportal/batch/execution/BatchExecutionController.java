package com.example.governanceportal.batch.execution;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/batch/jobs")
public class BatchExecutionController {

    private final BatchJobLauncherService batchJobLauncherService;

    public BatchExecutionController(BatchJobLauncherService batchJobLauncherService) {
        this.batchJobLauncherService = batchJobLauncherService;
    }

    // SSO/권한 적용 후에는 관리자 권한(예: BATCH_ADMIN)으로 제한해야 한다.
    @GetMapping
    public List<BatchJobSummary> listJobs() {
        return batchJobLauncherService.listJobs();
    }

    @PostMapping("/{jobName}/run")
    public BatchJobExecutionResponse runJob(
        @PathVariable String jobName,
        @RequestBody(required = false) BatchJobRunRequest request
    ) throws Exception {
        return batchJobLauncherService.run(jobName, request == null ? Map.of() : request.safeParameters());
    }

    @GetMapping("/executions/{executionId}")
    public ResponseEntity<BatchJobExecutionResponse> getExecution(@PathVariable long executionId) {
        return ResponseEntity.ok(batchJobLauncherService.getExecution(executionId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
    }
}
