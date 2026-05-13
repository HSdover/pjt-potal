package com.example.governanceportal.common.list;

import java.util.List;

public record ListResponse<TRow>(
    List<TRow> rows,
    long totalCount,
    int pageNo,
    int pageSize
) {
}
