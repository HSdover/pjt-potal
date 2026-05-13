package com.example.governanceportal.metadata.dto;

import com.example.governanceportal.common.list.ListSortRequest;
import java.util.List;

public record MetadataListRequest(
    Integer pageNo,
    Integer pageSize,
    List<ListSortRequest> sort,
    MetadataSearchFilter filters
) {
}
