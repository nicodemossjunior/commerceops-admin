package com.commerceops.admin.customers.service;

import com.commerceops.admin.common.error.DuplicateResourceException;
import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.customers.dto.CustomerFilter;
import com.commerceops.admin.customers.dto.CustomerRequest;
import com.commerceops.admin.customers.dto.CustomerResponse;
import com.commerceops.admin.customers.model.Customer;
import com.commerceops.admin.customers.repository.CustomerRepository;
import com.commerceops.admin.customers.repository.CustomerSpecifications;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CurrentUserProvider currentUserProvider;

    public CustomerService(CustomerRepository customerRepository, CurrentUserProvider currentUserProvider) {
        this.customerRepository = customerRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, null);
        Customer customer = new Customer(
                request.name().trim(),
                email,
                normalizeOptional(request.phone()),
                normalizeOptional(request.document()),
                request.status()
        );
        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID publicId) {
        return toResponse(findActive(publicId));
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(CustomerFilter filter, Pageable pageable) {
        return PageResponse.from(
                customerRepository.findAll(CustomerSpecifications.withFilters(filter), pageable).map(this::toResponse)
        );
    }

    @Transactional
    public CustomerResponse update(UUID publicId, CustomerRequest request) {
        Customer customer = findActive(publicId);
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, customer.getId());
        customer.update(
                request.name().trim(),
                email,
                normalizeOptional(request.phone()),
                normalizeOptional(request.document()),
                request.status()
        );
        return toResponse(customer);
    }

    @Transactional
    public void delete(UUID publicId) {
        Customer customer = findActive(publicId);
        customer.markDeleted(currentUserProvider.currentUser().id());
    }

    public Customer findActive(UUID publicId) {
        return customerRepository.findByPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer was not found."));
    }

    private void ensureEmailAvailable(String email, Long currentId) {
        if (email == null) {
            return;
        }
        boolean duplicate = currentId == null
                ? customerRepository.existsByEmailIgnoreCaseAndDeletedFalse(email)
                : customerRepository.existsByEmailIgnoreCaseAndDeletedFalseAndIdNot(email, currentId);
        if (duplicate) {
            throw new DuplicateResourceException("Customer email is already in use.");
        }
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeOptional(email);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getPublicId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDocument(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
