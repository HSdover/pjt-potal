package com.example.governanceportal.user.dto;

import java.util.List;

public record CurrentUser(
    String userId,
    String displayName,
    boolean authenticated,
    List<String> permissions
) {
}
