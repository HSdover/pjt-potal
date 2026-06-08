package com.example.governanceportal.metadata.integrated.dto;

import com.example.governanceportal.common.list.ListSortRequest;
import java.util.List;

public record IntegratedMetaListRequest(
    Integer pageNo,
    Integer pageSize,
    List<ListSortRequest> sort,
    IntegratedMetaSearchFilter filters
) {
}
