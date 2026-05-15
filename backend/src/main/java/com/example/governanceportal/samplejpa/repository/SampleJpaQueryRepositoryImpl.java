package com.example.governanceportal.samplejpa.repository;

import static com.example.governanceportal.samplejpa.domain.QSampleJpa.sampleJpa;

import com.example.governanceportal.samplejpa.domain.SampleJpa;
import com.example.governanceportal.samplejpa.dto.SampleJpaSearchFilter;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public class SampleJpaQueryRepositoryImpl implements SampleJpaQueryRepository {

    private final JPAQueryFactory queryFactory;

    public SampleJpaQueryRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<SampleJpa> search(SampleJpaSearchFilter filters, Pageable pageable) {
        Predicate keywordCondition = containsKeyword(filters);

        JPAQuery<SampleJpa> rowQuery = queryFactory
            .selectFrom(sampleJpa)
            .orderBy(toOrderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        JPAQuery<Long> countQuery = queryFactory
            .select(sampleJpa.count())
            .from(sampleJpa);

        if (keywordCondition != null) {
            rowQuery.where(keywordCondition);
            countQuery.where(keywordCondition);
        }

        List<SampleJpa> rows = rowQuery.fetch();
        Long totalCount = countQuery.fetchOne();

        return new PageImpl<>(rows, pageable, totalCount == null ? 0 : totalCount);
    }

    private Predicate containsKeyword(SampleJpaSearchFilter filters) {
        if (filters == null || !StringUtils.hasText(filters.keyword())) {
            return null;
        }

        String keyword = filters.keyword().trim();
        return sampleJpa.name.containsIgnoreCase(keyword)
            .or(sampleJpa.description.containsIgnoreCase(keyword));
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sort != null) {
            for (Sort.Order order : sort) {
                toOrderSpecifier(order).ifPresent(orderSpecifiers::add);
            }
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(sampleJpa.id.asc());
        }

        return orderSpecifiers.toArray(OrderSpecifier<?>[]::new);
    }

    private Optional<OrderSpecifier<?>> toOrderSpecifier(Sort.Order sort) {
        Order direction = sort.isDescending() ? Order.DESC : Order.ASC;
        return switch (sort.getProperty()) {
            case "id" -> Optional.of(new OrderSpecifier<>(direction, sampleJpa.id));
            case "name" -> Optional.of(new OrderSpecifier<>(direction, sampleJpa.name));
            case "description" -> Optional.of(new OrderSpecifier<>(direction, sampleJpa.description));
            default -> Optional.empty();
        };
    }
}
