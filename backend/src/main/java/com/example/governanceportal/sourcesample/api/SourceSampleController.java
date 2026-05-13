package com.example.governanceportal.sourcesample.api;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.sourcesample.dto.SourceSampleItem;
import com.example.governanceportal.sourcesample.dto.SourceSampleListRequest;
import com.example.governanceportal.sourcesample.service.SourceSampleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/source-sample")
public class SourceSampleController {

    private final SourceSampleService sourceSampleService;

    public SourceSampleController(SourceSampleService sourceSampleService) {
        this.sourceSampleService = sourceSampleService;
    }

    @GetMapping
    List<SourceSampleItem> findPreview() {
        return sourceSampleService.findPreview();
    }

    @PostMapping("/search")
    ListResponse<SourceSampleItem> search(@RequestBody SourceSampleListRequest request) {
        return sourceSampleService.search(request);
    }
}
