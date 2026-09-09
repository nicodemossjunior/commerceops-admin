package com.commerceops.admin.customers.dto;

import com.commerceops.admin.customers.model.CustomerStatus;

public record CustomerFilter(String name, String email, String phone, CustomerStatus status) {
}
