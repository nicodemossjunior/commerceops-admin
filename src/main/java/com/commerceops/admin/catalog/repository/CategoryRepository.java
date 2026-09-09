package com.commerceops.admin.catalog.repository;

import com.commerceops.admin.catalog.model.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByPublicIdAndDeletedFalse(UUID publicId);

    boolean existsBySlugAndDeletedFalse(String slug);
}
