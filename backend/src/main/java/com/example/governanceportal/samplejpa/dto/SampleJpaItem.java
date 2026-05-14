package com.example.governanceportal.samplejpa.dto;

import com.example.governanceportal.samplejpa.domain.SampleJpa;

public record SampleJpaItem(
    Long id,
    String name,
    String description
) {
    public static SampleJpaItem from(SampleJpa sample) {
        return new SampleJpaItem(sample.getId(), sample.getName(), sample.getDescription());
    }
}
