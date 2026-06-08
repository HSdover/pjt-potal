package com.example.governanceportal.reference.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

public record RefBoardSaveRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 50) String category,
    @NotBlank @Size(max = 50) String writerName,
    @NotBlank @Size(max = 4000) String content,
    @Valid RefBoardAttachment attachment
) {
}
