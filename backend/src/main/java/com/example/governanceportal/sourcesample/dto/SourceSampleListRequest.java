package com.example.governanceportal.sourcesample.dto;

import com.example.governanceportal.common.list.ListSortRequest;
import java.util.List;

public record SourceSampleListRequest(
    Integer pageNo,
    Integer pageSize,
    List<ListSortRequest> sort,
    SourceSampleSearchFilter filters
) {
}
