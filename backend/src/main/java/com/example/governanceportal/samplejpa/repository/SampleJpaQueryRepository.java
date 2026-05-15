package com.example.governanceportal.samplejpa.repository;

import com.example.governanceportal.samplejpa.domain.SampleJpa;
import com.example.governanceportal.samplejpa.dto.SampleJpaSearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SampleJpaQueryRepository {

    Page<SampleJpa> search(SampleJpaSearchFilter filters, Pageable pageable);
}
