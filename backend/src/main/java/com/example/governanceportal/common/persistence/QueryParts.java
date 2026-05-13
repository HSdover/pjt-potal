package com.example.governanceportal.common.persistence;

import java.util.Map;

public record QueryParts(
    String where,
    Map<String, Object> params
) {
}
