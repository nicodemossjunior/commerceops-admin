package com.commerceops.admin.catalog.repository;

import com.commerceops.admin.catalog.model.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByPublicIdAndDeletedFalse(UUID publicId);

    boolean existsBySlugAndDeletedFalse(String slug);

    boolean existsBySlugAndDeletedFalseAndIdNot(String slug, Long id);

    Page<Category> findAllByDeletedFalse(Pageable pageable);
}
