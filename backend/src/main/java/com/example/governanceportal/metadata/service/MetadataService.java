package com.example.governanceportal.metadata.service;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.metadata.dto.MetadataItem;
import com.example.governanceportal.metadata.dto.MetadataListRequest;
import com.example.governanceportal.metadata.repository.MetadataRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {

    private final MetadataRepository metadataRepository;

    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public List<MetadataItem> findAll() {
        return metadataRepository.findAll();
    }

    public ListResponse<MetadataItem> search(MetadataListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request.pageSize());
        int offset = PageSupport.offset(pageNo, pageSize);

        long totalCount = metadataRepository.count(request.filters());
        List<MetadataItem> rows = metadataRepository.search(request.filters(), request.sort(), offset, pageSize);

        return new ListResponse<>(rows, totalCount, pageNo, pageSize);
    }
}
