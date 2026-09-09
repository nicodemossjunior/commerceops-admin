package com.commerceops.admin.customers.model;

import com.commerceops.admin.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 190)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 50)
    private String document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerStatus status;

    protected Customer() {
    }

    public Customer(String name, String email, String phone, String document, CustomerStatus status) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
        this.status = status;
    }

    public void update(String name, String email, String phone, String document, CustomerStatus status) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDocument() {
        return document;
    }

    public CustomerStatus getStatus() {
        return status;
    }
}
