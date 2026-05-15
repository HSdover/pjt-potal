package com.example.governanceportal.samplejpa.repository;

import com.example.governanceportal.samplejpa.domain.SampleJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleJpaRepository extends JpaRepository<SampleJpa, Long>, SampleJpaQueryRepository {
}
