package com.commerceops.admin.dashboard;

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
import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.model.SalesOrder;
import com.commerceops.admin.orders.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardRepositoryTest {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void aggregatesOrdersAndReturnsNonDeletedLowStockProducts() {
        Customer customer = customerRepository.saveAndFlush(
                new Customer("Repository Customer", "dashboard-repository@example.com", null, null,
                        CustomerStatus.ACTIVE)
        );
        Category category = categoryRepository.saveAndFlush(
                new Category("Dashboard Repository", "dashboard-repository", null, CategoryStatus.ACTIVE, null)
        );
        Product product = productRepository.saveAndFlush(
                new Product(category, "DASH-REPOSITORY", "Repository Product", "dashboard-repository-product",
                        null, new BigDecimal("60.00"), null, 4, ProductStatus.ACTIVE)
        );
        saveOrder(customer, product, "DASH-REPOSITORY-PAID", OrderStatus.PAID);
        saveOrder(customer, product, "DASH-REPOSITORY-REFUNDED", OrderStatus.REFUNDED);

        var metrics = salesOrderRepository.summarizeDashboardMetrics(
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(60),
                OrderStatus.CANCELLED,
                OrderStatus.REFUNDED
        );
        var lowStock = productRepository
                .findByStockQuantityLessThanEqualAndDeletedFalseOrderByStockQuantityAscNameAsc(
                        10,
                        PageRequest.of(0, 5)
                );

        assertThat(metrics.getGrossRevenue()).isEqualByComparingTo("120.00");
        assertThat(metrics.getNetRevenue()).isEqualByComparingTo("60.00");
        assertThat(metrics.getOrderCount()).isEqualTo(2);
        assertThat(metrics.getRefundedOrderCount()).isEqualTo(1);
        assertThat(lowStock.getContent()).extracting(Product::getSku).containsExactly("DASH-REPOSITORY");
    }

    private void saveOrder(Customer customer, Product product, String orderNumber, OrderStatus status) {
        BigDecimal amount = new BigDecimal("60.00");
        SalesOrder order = new SalesOrder(
                customer,
                orderNumber,
                amount,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                amount
        );
        order.addItem(product, product.getSku(), product.getName(), amount, 1);
        order.changeStatus(status);
        salesOrderRepository.saveAndFlush(order);
    }
}
