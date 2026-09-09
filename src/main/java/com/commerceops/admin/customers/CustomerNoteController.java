package com.commerceops.admin.customers;

import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.customers.dto.CustomerNoteRequest;
import com.commerceops.admin.customers.dto.CustomerNoteResponse;
import com.commerceops.admin.customers.service.CustomerNoteService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerPublicId}/notes")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
@Tag(name = "Customer Notes", description = "Restricted internal notes for customer support")
@SecurityRequirement(name = "bearerAuth")
public class CustomerNoteController {

    private final CustomerNoteService customerNoteService;

    public CustomerNoteController(CustomerNoteService customerNoteService) {
        this.customerNoteService = customerNoteService;
    }

    @GetMapping
    @Operation(summary = "List internal customer notes")
    public PageResponse<CustomerNoteResponse> list(
            @PathVariable UUID customerPublicId,
            Pageable pageable
    ) {
        return customerNoteService.list(customerPublicId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an internal customer note")
    public CustomerNoteResponse create(
            @PathVariable UUID customerPublicId,
            @Valid @RequestBody CustomerNoteRequest request
    ) {
        return customerNoteService.create(customerPublicId, request);
    }

    @DeleteMapping("/{notePublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete an internal customer note")
    public void delete(
            @PathVariable UUID customerPublicId,
            @PathVariable UUID notePublicId
    ) {
        customerNoteService.delete(customerPublicId, notePublicId);
    }
}
