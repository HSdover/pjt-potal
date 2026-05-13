package com.example.governanceportal.sample.dto;

import com.example.governanceportal.common.list.ListSortRequest;
import java.util.List;

public record SampleListRequest(
    Integer pageNo,
    Integer pageSize,
    List<ListSortRequest> sort,
    SampleSearchFilter filters
) {
}
