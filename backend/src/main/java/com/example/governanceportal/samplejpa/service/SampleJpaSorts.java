package com.example.governanceportal.samplejpa.service;

import com.example.governanceportal.common.list.ListSortRequest;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

final class SampleJpaSorts {

    private SampleJpaSorts() {
    }

    static Sort toSort(List<ListSortRequest> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Order.asc("id"));
        }

        List<Sort.Order> orders = sort.stream()
            .map(SampleJpaSorts::toOrder)
            .filter(order -> order != null)
            .toList();

        if (orders.isEmpty()) {
            return Sort.by(Sort.Order.asc("id"));
        }

        return Sort.by(orders);
    }

    private static Sort.Order toOrder(ListSortRequest sort) {
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
}
