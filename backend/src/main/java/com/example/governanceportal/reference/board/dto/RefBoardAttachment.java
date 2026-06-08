package com.example.governanceportal.reference.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RefBoardAttachment(
    @NotBlank @Size(max = 36) String attachmentId,
    @NotBlank @Size(max = 255) String fileName,
    @PositiveOrZero long size,
    @NotBlank @Size(max = 255) String contentType,
    @NotBlank @Size(max = 500) String downloadUrl
) {
}
