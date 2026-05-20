package com.example.governanceportal.batch.execution;

import java.util.Map;

public record BatchJobRunRequest(
    Map<String, String> parameters
) {

    public Map<String, String> safeParameters() {
        return parameters == null ? Map.of() : parameters;
    }
}
