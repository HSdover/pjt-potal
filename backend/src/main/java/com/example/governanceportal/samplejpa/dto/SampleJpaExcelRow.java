package com.example.governanceportal.samplejpa.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class SampleJpaExcelRow {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("Name")
    private String name;

    @ExcelProperty("Description")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBlank() {
        return id == null && isBlank(name) && isBlank(description);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
