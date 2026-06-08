package com.example.governanceportal.common.excel;

public record ExcelImportError(
    int rowIndex,
    String column,
    String message
) {
}
