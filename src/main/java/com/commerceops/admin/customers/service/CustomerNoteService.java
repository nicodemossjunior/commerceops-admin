package com.commerceops.admin.customers.service;

import com.commerceops.admin.common.error.ResourceNotFoundException;
import com.commerceops.admin.common.pagination.PageResponse;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.customers.dto.CustomerNoteRequest;
import com.commerceops.admin.customers.dto.CustomerNoteResponse;
import com.commerceops.admin.customers.model.Customer;
import com.commerceops.admin.customers.model.CustomerNote;
import com.commerceops.admin.customers.repository.CustomerNoteRepository;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerNoteService {

    private final CustomerService customerService;
    private final CustomerNoteRepository customerNoteRepository;
    private final CurrentUserProvider currentUserProvider;

    public CustomerNoteService(
            CustomerService customerService,
            CustomerNoteRepository customerNoteRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.customerService = customerService;
        this.customerNoteRepository = customerNoteRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public CustomerNoteResponse create(UUID customerPublicId, CustomerNoteRequest request) {
        Customer customer = customerService.findActive(customerPublicId);
        CustomerNote note = new CustomerNote(customer, request.note().trim(), currentUserProvider.currentUser().id());
        return toResponse(customerNoteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerNoteResponse> list(UUID customerPublicId, Pageable pageable) {
        Customer customer = customerService.findActive(customerPublicId);
        return PageResponse.from(
                customerNoteRepository.findAllByCustomerIdAndDeletedFalse(customer.getId(), pageable)
                        .map(this::toResponse)
        );
    }

    @Transactional
    public void delete(UUID customerPublicId, UUID notePublicId) {
        Customer customer = customerService.findActive(customerPublicId);
        CustomerNote note = customerNoteRepository
                .findByPublicIdAndCustomerIdAndDeletedFalse(notePublicId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer note was not found."));
        note.markDeleted(currentUserProvider.currentUser().id());
    }

    private CustomerNoteResponse toResponse(CustomerNote note) {
        return new CustomerNoteResponse(
                note.getPublicId(),
                note.getNote(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
