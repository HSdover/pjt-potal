package com.example.governanceportal.common.excel;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public final class ExcelResponseHeaders {

    public static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private ExcelResponseHeaders() {
    }

    public static HttpHeaders attachment(String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(XLSX_MEDIA_TYPE);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.xlsx\"; filename*=UTF-8''" + encoded);
        return headers;
    }
}
