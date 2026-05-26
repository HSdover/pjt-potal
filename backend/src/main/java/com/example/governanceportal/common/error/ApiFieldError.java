package com.example.governanceportal.common.error;

public record ApiFieldError(
    String field,
    String message
) {
}
