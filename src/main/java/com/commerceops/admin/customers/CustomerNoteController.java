package com.commerceops.admin.customers;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.customers.dto.CustomerNoteRequest;
import com.commerceops.admin.customers.dto.CustomerNoteResponse;
import com.commerceops.admin.customers.service.CustomerNoteService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerPublicId}/notes")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
public class CustomerNoteController {

    private final CustomerNoteService customerNoteService;

    public CustomerNoteController(CustomerNoteService customerNoteService) {
        this.customerNoteService = customerNoteService;
    }

    @GetMapping
    public PageResponse<CustomerNoteResponse> list(
            @PathVariable UUID customerPublicId,
            Pageable pageable
    ) {
        return customerNoteService.list(customerPublicId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerNoteResponse create(
            @PathVariable UUID customerPublicId,
            @Valid @RequestBody CustomerNoteRequest request
    ) {
        return customerNoteService.create(customerPublicId, request);
    }

    @DeleteMapping("/{notePublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID customerPublicId,
            @PathVariable UUID notePublicId
    ) {
        customerNoteService.delete(customerPublicId, notePublicId);
    }
}
