package com.example.governanceportal.sample.service;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.sample.dto.SampleItem;
import com.example.governanceportal.sample.dto.SampleListRequest;
import com.example.governanceportal.sample.repository.SampleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

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
}
