package com.example.governanceportal.batch.execution;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/batch/jobs")
@PreAuthorize("@portalPermissionService.hasPermission(authentication, 'BATCH_ADMIN')")
public class BatchExecutionController {

    private final BatchJobLauncherService batchJobLauncherService;

    public BatchExecutionController(BatchJobLauncherService batchJobLauncherService) {
        this.batchJobLauncherService = batchJobLauncherService;
    }

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
}
