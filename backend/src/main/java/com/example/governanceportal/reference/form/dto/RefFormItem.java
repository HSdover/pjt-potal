package com.example.governanceportal.reference.form.dto;

import java.time.LocalDate;

public record RefFormItem(
    Long id,
    String name,
    String category,
    String description,
    LocalDate targetDate,
    String priority
) {
}
