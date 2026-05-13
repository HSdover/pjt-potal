package com.example.governanceportal.sample.api;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.sample.dto.SampleItem;
import com.example.governanceportal.sample.dto.SampleListRequest;
import com.example.governanceportal.sample.service.SampleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/samples")
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @PostMapping("/search")
    ListResponse<SampleItem> search(@RequestBody SampleListRequest request) {
        return sampleService.search(request);
    }
}
