package com.example.governanceportal.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LocalLoginRequest(
    @NotBlank String userId,
    @NotBlank String password
) {
}
