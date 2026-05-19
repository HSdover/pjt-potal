package com.example.governanceportal.reference.form.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RefFormSaveRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank String category,
    @Size(max = 1000) String description,
    LocalDate targetDate,
    String priority
) {
}
