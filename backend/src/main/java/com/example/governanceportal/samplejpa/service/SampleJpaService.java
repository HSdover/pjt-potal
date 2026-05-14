package com.example.governanceportal.samplejpa.service;

import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.ListSortRequest;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.samplejpa.domain.SampleJpa;
import com.example.governanceportal.samplejpa.dto.SampleJpaCreateRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaItem;
import com.example.governanceportal.samplejpa.dto.SampleJpaListRequest;
import com.example.governanceportal.samplejpa.dto.SampleJpaSearchFilter;
import com.example.governanceportal.samplejpa.dto.SampleJpaUpdateRequest;
import com.example.governanceportal.samplejpa.repository.SampleJpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

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
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, toSort(request.sort()));

        Page<SampleJpa> page = searchPage(request.filters(), pageable);
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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample JPA row was not found."));

        sample.update(normalizeRequiredName(request.name()), normalizeText(request.description()));
        return SampleJpaItem.from(sampleJpaRepository.saveAndFlush(sample));
    }

    @Transactional
    public void delete(Long id) {
        if (!sampleJpaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample JPA row was not found.");
        }

        sampleJpaRepository.deleteById(id);
    }

    private Page<SampleJpa> searchPage(SampleJpaSearchFilter filters, Pageable pageable) {
        if (filters == null || !StringUtils.hasText(filters.keyword())) {
            return sampleJpaRepository.findAll(pageable);
        }

        String keyword = filters.keyword().trim();
        return sampleJpaRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            keyword,
            keyword,
            pageable
        );
    }

    private Sort toSort(List<ListSortRequest> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Order.asc("id"));
        }

        List<Sort.Order> orders = sort.stream()
            .map(this::toOrder)
            .filter(order -> order != null)
            .toList();

        if (orders.isEmpty()) {
            return Sort.by(Sort.Order.asc("id"));
        }

        return Sort.by(orders);
    }

    private Sort.Order toOrder(ListSortRequest sort) {
        if (sort == null || !StringUtils.hasText(sort.field())) {
            return null;
        }

        String property = switch (sort.field()) {
            case "id" -> "id";
            case "name" -> "name";
            case "description" -> "description";
            default -> "";
        };

        if (!StringUtils.hasText(property)) {
            return null;
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sort.direction()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return new Sort.Order(direction, property);
    }

    private String normalizeRequiredName(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sample JPA name is required.");
        }

        return value.trim();
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
