package com.example.governanceportal.samplejpa.dto;

import com.example.governanceportal.common.list.ListSortRequest;
import java.util.List;

public record SampleJpaListRequest(
    Integer pageNo,
    Integer pageSize,
    List<ListSortRequest> sort,
    SampleJpaSearchFilter filters
) {
}
