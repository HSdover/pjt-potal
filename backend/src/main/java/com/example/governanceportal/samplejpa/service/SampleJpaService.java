package com.example.governanceportal.samplejpa.service;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.samplejpa.domain.SampleJpa;
import com.example.governanceportal.samplejpa.dto.SampleJpaCreateRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaItem;
import com.example.governanceportal.samplejpa.dto.SampleJpaListRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaUpdateRequest;
import com.example.governanceportal.samplejpa.repository.SampleJpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SampleJpaService {

    private final SampleJpaRepository sampleJpaRepository;

    public SampleJpaService(SampleJpaRepository sampleJpaRepository) {
        this.sampleJpaRepository = sampleJpaRepository;
    }

    @Transactional(readOnly = true)
    public ListResponse<SampleJpaItem> search(SampleJpaListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request.pageSize());
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, SampleJpaSorts.toSort(request.sort()));

        Page<SampleJpa> page = sampleJpaRepository.search(request.filters(), pageable);
        List<SampleJpaItem> rows = page.getContent().stream()
            .map(SampleJpaItem::from)
            .toList();

        return new ListResponse<>(rows, page.getTotalElements(), pageNo, pageSize);
    }

    @Transactional
    public SampleJpaItem create(SampleJpaCreateRequest request) {
        SampleJpa sample = new SampleJpa(normalizeRequiredName(request.name()), normalizeText(request.description()));
        return SampleJpaItem.from(sampleJpaRepository.saveAndFlush(sample));
    }

    @Transactional
    public SampleJpaItem update(Long id, SampleJpaUpdateRequest request) {
        SampleJpa sample = sampleJpaRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound("Sample JPA row was not found."));

        sample.update(normalizeRequiredName(request.name()), normalizeText(request.description()));
        return SampleJpaItem.from(sampleJpaRepository.saveAndFlush(sample));
    }

    @Transactional
    public void delete(Long id) {
        if (!sampleJpaRepository.existsById(id)) {
            throw BusinessException.notFound("Sample JPA row was not found.");
        }

        sampleJpaRepository.deleteById(id);
    }

    private String normalizeRequiredName(String value) {
        if (!StringUtils.hasText(value)) {
            throw BusinessException.badRequest("Sample JPA name is required.");
        }

        return value.trim();
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
