package com.commerceops.admin.customers;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.customers.dto.CustomerFilter;
import com.commerceops.admin.customers.dto.CustomerRequest;
import com.commerceops.admin.customers.dto.CustomerResponse;
import com.commerceops.admin.customers.dto.CustomerOrderSummaryResponse;
import com.commerceops.admin.customers.model.CustomerStatus;
import com.commerceops.admin.customers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Customers", description = "Customer records, search, and purchase history")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private static final String READ_ROLES = "hasAnyRole('ADMIN', 'MANAGER', 'SUPPORT', 'READ_ONLY', 'CATALOG')";
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize(READ_ROLES)
    @Operation(summary = "List and filter customers", description = "Returns non-deleted customers with pagination.")
    public PageResponse<CustomerResponse> list(
            @Parameter(description = "Case-insensitive partial customer name")
            @RequestParam(required = false) String name,
            @Parameter(description = "Case-insensitive partial customer email")
            @RequestParam(required = false) String email,
            @Parameter(description = "Partial customer phone")
            @RequestParam(required = false) String phone,
            @Parameter(description = "Customer status")
            @RequestParam(required = false) CustomerStatus status,
            Pageable pageable
    ) {
        return customerService.list(new CustomerFilter(name, email, phone, status), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a customer")
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize(READ_ROLES)
    @Operation(summary = "Get a customer by public ID")
    public CustomerResponse get(@PathVariable UUID publicId) {
        return customerService.get(publicId);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update a customer")
    public CustomerResponse update(
            @PathVariable UUID publicId,
            @Valid @RequestBody CustomerRequest request
    ) {
        return customerService.update(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Soft delete a customer")
    public void delete(@PathVariable UUID publicId) {
        customerService.delete(publicId);
    }

    @GetMapping("/{publicId}/orders")
    @PreAuthorize(READ_ROLES)
    @Operation(
            summary = "List customer purchase history",
            description = "Returns the customer's non-deleted orders with pagination."
    )
    public PageResponse<CustomerOrderSummaryResponse> purchaseHistory(
            @PathVariable UUID publicId,
            Pageable pageable
    ) {
        return customerService.purchaseHistory(publicId, pageable);
    }
}
