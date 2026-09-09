package com.commerceops.admin.customers.model;

import com.commerceops.admin.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_note")
public class CustomerNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 4000)
    private String note;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    protected CustomerNote() {
    }

    public CustomerNote(Customer customer, String note, Long createdBy) {
        this.customer = customer;
        this.note = note;
        this.createdBy = createdBy;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getNote() {
        return note;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
