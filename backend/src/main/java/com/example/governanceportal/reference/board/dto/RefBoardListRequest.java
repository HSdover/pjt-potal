package com.example.governanceportal.reference.board.dto;

import com.example.governanceportal.common.list.ListSortRequest;
import java.util.List;

public record RefBoardListRequest(
    Integer pageNo,
    Integer pageSize,
    List<ListSortRequest> sort,
    RefBoardSearchFilter filters
) {
}
