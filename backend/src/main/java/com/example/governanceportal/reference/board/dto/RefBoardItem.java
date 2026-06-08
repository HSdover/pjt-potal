package com.example.governanceportal.reference.board.dto;

import java.time.LocalDateTime;

public record RefBoardItem(
    Long id,
    String title,
    String category,
    String writerName,
    String content,
    RefBoardAttachment attachment,
    int viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
