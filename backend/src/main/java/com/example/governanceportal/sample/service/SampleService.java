package com.example.governanceportal.sample.service;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.sample.dto.SampleCreateRequest;
import com.example.governanceportal.sample.dto.SampleItem;
import com.example.governanceportal.sample.dto.SampleListRequest;
import com.example.governanceportal.sample.dto.SampleUpdateRequest;
import com.example.governanceportal.sample.repository.SampleRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SampleService {

    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public ListResponse<SampleItem> search(SampleListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request.pageSize());
        int offset = PageSupport.offset(pageNo, pageSize);

        long totalCount = sampleRepository.count(request.filters());
        List<SampleItem> rows = sampleRepository.search(request.filters(), request.sort(), offset, pageSize);

        return new ListResponse<>(rows, totalCount, pageNo, pageSize);
    }

    public SampleItem create(SampleCreateRequest request) {
        String name = normalizeRequiredName(request.name());
        String description = normalizeText(request.description());
        Long id = sampleRepository.create(name, description);

        return sampleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Created sample was not found."));
    }

    public SampleItem update(Long id, SampleUpdateRequest request) {
        String name = normalizeRequiredName(request.name());
        String description = normalizeText(request.description());

        boolean updated = sampleRepository.update(id, name, description);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample was not found.");
        }

        return sampleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample was not found."));
    }

    public void delete(Long id) {
        boolean deleted = sampleRepository.delete(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample was not found.");
        }
    }

    private String normalizeRequiredName(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sample name is required.");
        }

        return value.trim();
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
