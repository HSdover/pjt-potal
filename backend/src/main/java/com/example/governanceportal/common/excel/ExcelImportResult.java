package com.example.governanceportal.common.excel;

import java.util.List;

public record ExcelImportResult(
    int totalRows,
    int successRows,
    int errorRows,
    List<ExcelImportError> errors
) {
    public static ExcelImportResult success(int totalRows) {
        return new ExcelImportResult(totalRows, totalRows, 0, List.of());
    }

    public static ExcelImportResult failed(int totalRows, List<ExcelImportError> errors) {
        List<ExcelImportError> safeErrors = errors == null ? List.of() : List.copyOf(errors);
        return new ExcelImportResult(totalRows, 0, safeErrors.size(), safeErrors);
    }
}
