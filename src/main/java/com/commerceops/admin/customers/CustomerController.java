package com.commerceops.admin.customers;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.customers.dto.CustomerFilter;
import com.commerceops.admin.customers.dto.CustomerRequest;
import com.commerceops.admin.customers.dto.CustomerResponse;
import com.commerceops.admin.customers.model.CustomerStatus;
import com.commerceops.admin.customers.service.CustomerService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'SUPPORT', 'READ_ONLY', 'CATALOG')";
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    public PageResponse<CustomerResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) CustomerStatus status,
            Pageable pageable
    ) {
        return customerService.list(new CustomerFilter(name, email, phone, status), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize(READ_ROLES)
    public CustomerResponse get(@PathVariable UUID publicId) {
        return customerService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public CustomerResponse update(
            @PathVariable UUID publicId,
            @Valid @RequestBody CustomerRequest request
    ) {
        return customerService.update(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void delete(@PathVariable UUID publicId) {
        customerService.delete(publicId);
    }
}
