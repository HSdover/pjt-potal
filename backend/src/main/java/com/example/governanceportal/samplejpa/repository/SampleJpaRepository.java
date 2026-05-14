package com.example.governanceportal.samplejpa.repository;

import com.example.governanceportal.samplejpa.domain.SampleJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleJpaRepository extends JpaRepository<SampleJpa, Long> {

    Page<SampleJpa> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name,
        String description,
        Pageable pageable
    );
}
