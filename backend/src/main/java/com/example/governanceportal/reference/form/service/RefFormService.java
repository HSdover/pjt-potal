package com.example.governanceportal.reference.form.service;

import com.example.governanceportal.reference.form.dto.RefFormItem;
import com.example.governanceportal.reference.form.dto.RefFormSaveRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class RefFormService {

    private final List<RefFormItem> items = new ArrayList<>(List.of(
        new RefFormItem(1L, "데이터 카탈로그 신규 등록", "기준정보", "신규 도입 데이터셋의 카탈로그 등록 요청", LocalDate.of(2026, 6, 15), "HIGH"),
        new RefFormItem(2L, "고객 등급 기준 변경", "정책", "고객 등급 산정 기준 변경 요청", LocalDate.of(2026, 7, 1), "NORMAL")
    ));

    private final AtomicLong sequence = new AtomicLong(items.size());

    public List<RefFormItem> findAll() {
        return List.copyOf(items);
    }

    public RefFormItem findById(Long id) {
        return items.stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Reference form not found: " + id));
    }

    public RefFormItem create(RefFormSaveRequest request) {
        RefFormItem item = new RefFormItem(
            sequence.incrementAndGet(),
            request.name(),
            request.category(),
            request.description(),
            request.targetDate(),
            request.priority()
        );
        items.add(item);
        return item;
    }

    public RefFormItem update(Long id, RefFormSaveRequest request) {
        RefFormItem existing = findById(id);
        items.remove(existing);
        RefFormItem updated = new RefFormItem(
            id,
            request.name(),
            request.category(),
            request.description(),
            request.targetDate(),
            request.priority()
        );
        items.add(updated);
        return updated;
    }
}
