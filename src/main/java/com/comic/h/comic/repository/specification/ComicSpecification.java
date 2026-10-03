package com.comic.h.comic.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.entity.Genre;
import com.comic.h.comic.enums.ComicStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class ComicSpecification {

    public static Specification<Comic> filter(
            String query,
            String genreSlug,
            List<String> genreSlugs,
            ComicStatus status,
            String uploader) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by keyword in title or author
            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), pattern);
                Predicate authorPredicate = cb.like(cb.lower(root.get("author")), pattern);
                predicates.add(cb.or(titlePredicate, authorPredicate));
            }

            // Filter by multiple genre slugs or single genre slug
            if (genreSlugs != null && !genreSlugs.isEmpty()) {
                Join<Comic, Genre> genreJoin = root.join("genres", JoinType.INNER);
                predicates.add(genreJoin.get("slug").in(genreSlugs));
                if (cq != null) {
                    cq.distinct(true);
                }
            } else if (genreSlug != null && !genreSlug.trim().isEmpty() && !"ALL".equalsIgnoreCase(genreSlug.trim())) {
                Join<Comic, Genre> genreJoin = root.join("genres", JoinType.INNER);
                predicates.add(cb.equal(genreJoin.get("slug"), genreSlug.trim()));
                if (cq != null) {
                    cq.distinct(true);
                }
            }

            // Filter by status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter by uploader
            if (uploader != null && !uploader.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("uploader").get("username"), uploader.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
