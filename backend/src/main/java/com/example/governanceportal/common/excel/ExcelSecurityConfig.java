package com.example.governanceportal.common.excel;

import jakarta.annotation.PostConstruct;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.util.IOUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelSecurityConfig {

    @PostConstruct
    void configurePoiSecurity() {
        ZipSecureFile.setMinInflateRatio(0.005);
        IOUtils.setByteArrayMaxOverride(50_000_000);
    }
}
