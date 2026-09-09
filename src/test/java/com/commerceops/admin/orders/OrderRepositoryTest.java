package com.commerceops.admin.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.model.CategoryStatus;
import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.model.ProductStatus;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.customers.model.Customer;
import com.commerceops.admin.customers.model.CustomerStatus;
import com.commerceops.admin.customers.repository.CustomerRepository;
import com.commerceops.admin.orders.dto.OrderFilter;
import com.commerceops.admin.orders.model.DeliveryStatus;
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.PaymentStatus;
import com.commerceops.admin.orders.model.SalesOrder;
import com.commerceops.admin.orders.repository.OrderSpecifications;
import com.commerceops.admin.orders.repository.SalesOrderRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryTest {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void combinesFiltersAndExcludesDeletedOrders() {
        Customer alice = saveCustomer("Alice", "alice@example.com");
        Product product = saveProduct();
        SalesOrder matching = saveOrder(alice, product, "ORD-1001", OrderStatus.PAID);
        saveOrder(saveCustomer("Bob", "bob@example.com"), product, "ORD-2001", OrderStatus.PENDING);
        SalesOrder deleted = saveOrder(alice, product, "ORD-1002", OrderStatus.PAID);
        deleted.markDeleted(1L);
        salesOrderRepository.flush();

        OrderFilter filter = new OrderFilter(
                "ord-10",
                alice.getPublicId(),
                OrderStatus.PAID,
                PaymentStatus.PAID,
                DeliveryStatus.PENDING,
                matching.getCreatedAt().minusSeconds(1),
                matching.getCreatedAt().plusSeconds(1)
        );
        var result = salesOrderRepository.findAll(
                OrderSpecifications.withFilters(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(SalesOrder::getOrderNumber).containsExactly("ORD-1001");
    }

    private Customer saveCustomer(String name, String email) {
        return customerRepository.saveAndFlush(
                new Customer(name, email, null, null, CustomerStatus.ACTIVE)
        );
    }

    private Product saveProduct() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Peripherals", "peripherals-orders", null, CategoryStatus.ACTIVE, null)
        );
        return productRepository.saveAndFlush(
                new Product(category, "SKU-ORDERS", "Operations Keyboard", "operations-keyboard-orders", null,
                        new BigDecimal("100.00"), null, 10, ProductStatus.ACTIVE)
        );
    }

    private SalesOrder saveOrder(
            Customer customer,
            Product product,
            String orderNumber,
            OrderStatus status
    ) {
        SalesOrder order = new SalesOrder(
                customer,
                orderNumber,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("95.00")
        );
        order.addItem(product, product.getSku(), product.getName(), new BigDecimal("100.00"), 1);
        if (status != OrderStatus.PENDING) {
            order.changeStatus(status);
        }
        return salesOrderRepository.saveAndFlush(order);
    }
}
