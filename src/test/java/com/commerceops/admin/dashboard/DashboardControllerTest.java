package com.commerceops.admin.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Test
    @WithMockUser(roles = "READ_ONLY")
    void returnsStableSummaryUsingDefaultPeriod() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.from").isString())
                .andExpect(jsonPath("$.period.to").isString())
                .andExpect(jsonPath("$.metrics.grossRevenue").value(0.00))
                .andExpect(jsonPath("$.metrics.netRevenue").value(0.00))
                .andExpect(jsonPath("$.metrics.orderCount").value(0))
                .andExpect(jsonPath("$.metrics.averageOrderValue").value(0.00))
                .andExpect(jsonPath("$.metrics.customerCount").value(0))
                .andExpect(jsonPath("$.metrics.cancelledOrderCount").value(0))
                .andExpect(jsonPath("$.metrics.refundedOrderCount").value(0))
                .andExpect(jsonPath("$.metrics.lowStockProductCount").value(0))
                .andExpect(jsonPath("$.recentOrders").isArray())
                .andExpect(jsonPath("$.lowStockProducts").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void calculatesMetricsAndSupportingListsForCustomPeriod() throws Exception {
        Customer activeCustomer = saveCustomer("Alice", "dashboard-alice@example.com");
        Customer secondCustomer = saveCustomer("Bob", "dashboard-bob@example.com");
        Customer deletedCustomer = saveCustomer("Deleted", "dashboard-deleted@example.com");
        deletedCustomer.markDeleted(1L);
        customerRepository.flush();

        Product orderProduct = saveProduct("DASH-ORDER", "Order Product", 50);
        Product lowestStock = saveProduct("DASH-LOW-1", "Lowest Stock", 2);
        saveProduct("DASH-LOW-2", "Threshold Stock", 10);
        saveProduct("DASH-HIGH", "Healthy Stock", 11);
        Product deletedLowStock = saveProduct("DASH-DELETED", "Deleted Stock", 1);
        deletedLowStock.markDeleted(1L);
        productRepository.flush();

        saveOrder(activeCustomer, orderProduct, "DASH-PAID", "100.00", OrderStatus.PAID);
        saveOrder(secondCustomer, orderProduct, "DASH-REFUNDED", "40.00", OrderStatus.REFUNDED);
        saveOrder(activeCustomer, orderProduct, "DASH-CANCELLED", "20.00", OrderStatus.CANCELLED);

        Instant from = Instant.now().minusSeconds(60);
        Instant to = Instant.now().plusSeconds(60);
        mockMvc.perform(get("/api/dashboard/summary")
                        .param("period", "CUSTOM")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.from").value(from.toString()))
                .andExpect(jsonPath("$.period.to").value(to.toString()))
                .andExpect(jsonPath("$.metrics.grossRevenue").value(140.00))
                .andExpect(jsonPath("$.metrics.netRevenue").value(100.00))
                .andExpect(jsonPath("$.metrics.orderCount").value(3))
                .andExpect(jsonPath("$.metrics.averageOrderValue").value(100.00))
                .andExpect(jsonPath("$.metrics.customerCount").value(2))
                .andExpect(jsonPath("$.metrics.cancelledOrderCount").value(1))
                .andExpect(jsonPath("$.metrics.refundedOrderCount").value(1))
                .andExpect(jsonPath("$.metrics.lowStockProductCount").value(2))
                .andExpect(jsonPath("$.recentOrders.length()").value(3))
                .andExpect(jsonPath("$.recentOrders[0].customerPublicId").isString())
                .andExpect(jsonPath("$.lowStockProducts.length()").value(2))
                .andExpect(jsonPath("$.lowStockProducts[0].publicId")
                        .value(lowestStock.getPublicId().toString()))
                .andExpect(jsonPath("$.lowStockProducts[0].stockQuantity").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsInvalidCustomAndUnknownPeriods() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .param("period", "CUSTOM")
                        .param("from", "2026-09-09T10:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/dashboard/summary")
                        .param("period", "CUSTOM")
                        .param("from", "2026-09-09T11:00:00Z")
                        .param("to", "2026-09-09T10:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/dashboard/summary").param("period", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private Customer saveCustomer(String name, String email) {
        return customerRepository.saveAndFlush(
                new Customer(name, email, null, null, CustomerStatus.ACTIVE)
        );
    }

    private Product saveProduct(String sku, String name, int stockQuantity) {
        Category category = categoryRepository.saveAndFlush(
                new Category(name + " Category", "dashboard-" + sku.toLowerCase(), null,
                        CategoryStatus.ACTIVE, null)
        );
        return productRepository.saveAndFlush(
                new Product(category, sku, name, "dashboard-" + sku.toLowerCase(), null,
                        new BigDecimal("100.00"), null, stockQuantity, ProductStatus.ACTIVE)
        );
    }

    private SalesOrder saveOrder(
            Customer customer,
            Product product,
            String orderNumber,
            String total,
            OrderStatus status
    ) {
        BigDecimal amount = new BigDecimal(total);
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
        return salesOrderRepository.saveAndFlush(order);
    }
}
