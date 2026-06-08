package com.example.governanceportal.common.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class ExcelExportSupport {

    public <T> void write(
        OutputStream outputStream,
        String sheetName,
        List<ExcelColumnSpec<T>> columns,
        Consumer<ExcelChunkWriter<T>> chunkWriter
    ) {
        ExcelWriter excelWriter = EasyExcel.write(outputStream)
            .head(headers(columns))
            .autoCloseStream(false)
            .build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

        try {
            chunkWriter.accept(rows -> excelWriter.write(values(columns, rows), writeSheet));
        } finally {
            excelWriter.finish();
        }
    }

    private <T> List<List<String>> headers(List<ExcelColumnSpec<T>> columns) {
        return columns.stream()
            .map(column -> List.of(column.headerName()))
            .toList();
    }

    private <T> List<List<Object>> values(List<ExcelColumnSpec<T>> columns, List<T> rows) {
        return rows.stream()
            .map(row -> columns.stream()
                .map(column -> safeCellValue(column.valueExtractor().apply(row)))
                .toList())
            .toList();
    }

    private Object safeCellValue(Object value) {
        if (!(value instanceof String text)) {
            return value;
        }

        if (text.isEmpty()) {
            return text;
        }

        char first = text.charAt(0);
        return switch (first) {
            case '=', '+', '-', '@', '\t' -> "'" + text;
            default -> text;
        };
    }

    @FunctionalInterface
    public interface ExcelChunkWriter<T> {
        void write(List<T> rows);
    }
}
