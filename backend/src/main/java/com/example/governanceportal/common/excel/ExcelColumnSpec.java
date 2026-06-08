package com.example.governanceportal.common.excel;

import java.util.function.Function;

public record ExcelColumnSpec<T>(
    String headerName,
    Function<T, ?> valueExtractor
) {
}
