package com.example.governanceportal.samplejpa.api;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.samplejpa.dto.SampleJpaCreateRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaItem;
import com.example.governanceportal.samplejpa.dto.SampleJpaListRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaUpdateRequest;
import com.example.governanceportal.samplejpa.service.SampleJpaService;
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
@RequestMapping("/api/samples-jpa")
public class SampleJpaController {

    private final SampleJpaService sampleJpaService;

    public SampleJpaController(SampleJpaService sampleJpaService) {
        this.sampleJpaService = sampleJpaService;
    }

    @PostMapping("/search")
    ListResponse<SampleJpaItem> search(@RequestBody SampleJpaListRequest request) {
        return sampleJpaService.search(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SampleJpaItem create(@RequestBody SampleJpaCreateRequest request) {
        return sampleJpaService.create(request);
    }

    @PutMapping("/{id}")
    SampleJpaItem update(@PathVariable Long id, @RequestBody SampleJpaUpdateRequest request) {
        return sampleJpaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        sampleJpaService.delete(id);
    }
}
