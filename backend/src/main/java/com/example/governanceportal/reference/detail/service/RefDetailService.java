package com.example.governanceportal.reference.detail.service;

import com.example.governanceportal.reference.detail.dto.RefDetailItem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RefDetailService {

    private static final List<RefDetailItem> ITEMS = List.of(
        new RefDetailItem(1L, "고객 등급 마스터", "MD-001", "기준정보", "데이터관리팀", "고객 등급 정의 및 기준 관리", "ACTIVE", LocalDateTime.of(2026, 1, 15, 10, 30)),
        new RefDetailItem(2L, "상품 분류 코드", "MD-002", "기준정보", "상품기획팀", "상품 카테고리 분류 체계", "ACTIVE", LocalDateTime.of(2026, 2, 3, 14, 12)),
        new RefDetailItem(3L, "거래 유형 정의", "MD-003", "기준정보", "리스크관리팀", "거래 분류 및 위험도 기준", "ARCHIVED", LocalDateTime.of(2026, 2, 18, 9, 5)),
        new RefDetailItem(4L, "지점 마스터", "MD-004", "조직", "운영지원팀", "지점 코드 및 관할 정보", "ACTIVE", LocalDateTime.of(2026, 3, 1, 11, 0))
    );

    public List<RefDetailItem> findAll() {
        return ITEMS;
    }

    public RefDetailItem findById(Long id) {
        return ITEMS.stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Reference detail not found: " + id));
    }
}
