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
import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 200)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.DRAFT;

    protected Product() {
    }

    public Product(
            Category category,
            String sku,
            String name,
            String slug,
            String description,
            BigDecimal price,
            String imageUrl,
            int stockQuantity,
            ProductStatus status
    ) {
        this.category = category;
        this.sku = sku;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public String getSku() {
        return sku;
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

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void update(
            Category category,
            String sku,
            String name,
            String slug,
            String description,
            BigDecimal price,
            String imageUrl,
            int stockQuantity,
            ProductStatus status
    ) {
        this.category = category;
        this.sku = sku;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
        this.status = status;
    }
}
