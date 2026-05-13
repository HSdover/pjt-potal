package com.example.governanceportal.sourcesample.service;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.sourcesample.dto.SourceSampleItem;
import com.example.governanceportal.sourcesample.dto.SourceSampleListRequest;
import com.example.governanceportal.sourcesample.repository.SourceSampleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SourceSampleService {

    private final SourceSampleRepository sourceSampleRepository;

    public SourceSampleService(SourceSampleRepository sourceSampleRepository) {
        this.sourceSampleRepository = sourceSampleRepository;
    }

    public List<SourceSampleItem> findPreview() {
        return sourceSampleRepository.findPreview();
    }

    public ListResponse<SourceSampleItem> search(SourceSampleListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request.pageSize());
        int offset = PageSupport.offset(pageNo, pageSize);

        long totalCount = sourceSampleRepository.count(request.filters());
        List<SourceSampleItem> rows = sourceSampleRepository.search(request.filters(), request.sort(), offset, pageSize);

        return new ListResponse<>(rows, totalCount, pageNo, pageSize);
    }
}
