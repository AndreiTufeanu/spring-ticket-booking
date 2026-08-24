package com.andreitufeanu.backend.event.specification;

import com.andreitufeanu.backend.event.entity.Category;
import com.andreitufeanu.backend.event.entity.Event;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> isUpcoming(Instant now) {
        return (root, query, cb) -> cb.greaterThan(root.get("eventDate"), now);
    }

    public static Specification<Event> hasAllCategories(List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) -> {
            Predicate[] predicates = categoryIds.stream()
                    .map(categoryId -> existsCategory(root, query, cb, categoryId))
                    .toArray(Predicate[]::new);

            return cb.and(predicates);
        };
    }

    private static Predicate existsCategory(
            Root<Event> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            UUID categoryId) {

        Subquery<UUID> subquery = query.subquery(UUID.class);
        Root<Event> correlatedRoot = subquery.correlate(root);
        Join<Event, Category> categoryJoin = correlatedRoot.join("categories");

        subquery.select(categoryJoin.get("id"))
                .where(cb.equal(categoryJoin.get("id"), categoryId));

        return cb.exists(subquery);
    }
}