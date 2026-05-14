package com.example.governanceportal.sample.api;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.sample.dto.SampleCreateRequest;
import com.example.governanceportal.sample.dto.SampleItem;
import com.example.governanceportal.sample.dto.SampleListRequest;
import com.example.governanceportal.sample.dto.SampleUpdateRequest;
import com.example.governanceportal.sample.service.SampleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SampleItem create(@RequestBody SampleCreateRequest request) {
        return sampleService.create(request);
    }

    @PutMapping("/{id}")
    SampleItem update(@PathVariable Long id, @RequestBody SampleUpdateRequest request) {
        return sampleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        sampleService.delete(id);
    }
}
