package com.commerceops.admin.customers;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerceops.admin.customers.dto.CustomerFilter;
import com.commerceops.admin.customers.model.Customer;
import com.commerceops.admin.customers.model.CustomerStatus;
import com.commerceops.admin.customers.repository.CustomerRepository;
import com.commerceops.admin.customers.repository.CustomerSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void combinesFiltersAndExcludesDeletedCustomers() {
        customerRepository.saveAndFlush(new Customer(
                "Alice Smith", "alice@example.com", "+55 85 99999-9999", "DOC-001", CustomerStatus.ACTIVE
        ));
        customerRepository.saveAndFlush(new Customer(
                "Bob Jones", "bob@example.com", "+55 85 98888-8888", "DOC-002", CustomerStatus.BLOCKED
        ));
        Customer deleted = new Customer(
                "Deleted Alice", "deleted@example.com", "+55 85 99999-0000", "DOC-003", CustomerStatus.ACTIVE
        );
        deleted.markDeleted(1L);
        customerRepository.saveAndFlush(deleted);

        CustomerFilter filter = new CustomerFilter("alice", "example.com", "+55 85", CustomerStatus.ACTIVE);
        var result = customerRepository.findAll(
                CustomerSpecifications.withFilters(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Customer::getEmail).containsExactly("alice@example.com");
    }
}
