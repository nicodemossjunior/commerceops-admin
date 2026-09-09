package com.commerceops.admin.catalog.model;

import com.commerceops.admin.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
public class Category extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 160)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoryStatus status = CategoryStatus.ACTIVE;

    protected Category() {
    }

    public Category(String name, String slug, String description, CategoryStatus status, Category parent) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.status = status;
        this.parent = parent;
    }

    public Category getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public CategoryStatus getStatus() {
        return status;
    }
}
