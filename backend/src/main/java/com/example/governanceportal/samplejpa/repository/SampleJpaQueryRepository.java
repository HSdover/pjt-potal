package com.example.governanceportal.samplejpa.repository;

import com.example.governanceportal.samplejpa.domain.SampleJpa;
import com.example.governanceportal.samplejpa.dto.SampleJpaSearchFilter;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SampleJpaQueryRepository {

    Page<SampleJpa> search(SampleJpaSearchFilter filters, Pageable pageable);

    List<SampleJpa> searchRows(SampleJpaSearchFilter filters, Pageable pageable);

    long countBySearchFilter(SampleJpaSearchFilter filters);
}
