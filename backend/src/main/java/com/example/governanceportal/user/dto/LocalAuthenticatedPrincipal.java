package com.example.governanceportal.user.dto;

import java.io.Serializable;
import java.util.List;

public record LocalAuthenticatedPrincipal(
    String userId,
    String displayName,
    List<String> permissions
) implements Serializable {

    public LocalAuthenticatedPrincipal {
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }
}
