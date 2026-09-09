package com.commerceops.admin.orders;

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
import com.commerceops.admin.orders.model.SalesOrder;
import com.commerceops.admin.orders.repository.SalesOrderRepository;
import java.math.BigDecimal;
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
class OrderControllerTest {

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
    void listsOrdersWithFiltersAndPagination() throws Exception {
        Customer customer = saveCustomer("Alice Smith");
        saveOrder(customer, "ORD-1001");
        saveOrder(saveCustomer("Bob Jones"), "ORD-2001");

        mockMvc.perform(get("/api/orders")
                        .param("orderNumber", "1001")
                        .param("customerId", customer.getPublicId().toString())
                        .param("status", "PENDING")
                        .param("paymentStatus", "PENDING")
                        .param("deliveryStatus", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-1001"))
                .andExpect(jsonPath("$.content[0].customerPublicId")
                        .value(customer.getPublicId().toString()));
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void returnsOrderDetailWithCustomerItemsAndTotals() throws Exception {
        SalesOrder order = saveOrder(saveCustomer("Alice Smith"), "ORD-1001");

        mockMvc.perform(get("/api/orders/{publicId}", order.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-1001"))
                .andExpect(jsonPath("$.customerName").value("Alice Smith"))
                .andExpect(jsonPath("$.subtotalAmount").value(100.00))
                .andExpect(jsonPath("$.discountAmount").value(10.00))
                .andExpect(jsonPath("$.shippingAmount").value(5.00))
                .andExpect(jsonPath("$.totalAmount").value(95.00))
                .andExpect(jsonPath("$.items[0].productSku").value("SKU-001"))
                .andExpect(jsonPath("$.items[0].productName").value("Operations Keyboard"))
                .andExpect(jsonPath("$.statusHistory.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    void exposesOrdersInCustomerPurchaseHistory() throws Exception {
        Customer customer = saveCustomer("Alice Smith");
        saveOrder(customer, "ORD-1001");

        mockMvc.perform(get("/api/customers/{publicId}/orders", customer.getPublicId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-1001"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].totalAmount").value(95.00));
    }

    private Customer saveCustomer(String name) {
        return customerRepository.saveAndFlush(
                new Customer(name, name.toLowerCase().replace(' ', '.') + "@example.com", null, null,
                        CustomerStatus.ACTIVE)
        );
    }

    private SalesOrder saveOrder(Customer customer, String orderNumber) {
        Category category = categoryRepository.saveAndFlush(
                new Category("Peripherals " + orderNumber, "peripherals-" + orderNumber.toLowerCase(), null,
                        CategoryStatus.ACTIVE, null)
        );
        Product product = productRepository.saveAndFlush(
                new Product(category, "SKU-001-" + orderNumber, "Operations Keyboard",
                        "operations-keyboard-" + orderNumber.toLowerCase(), null, new BigDecimal("100.00"), null,
                        10, ProductStatus.ACTIVE)
        );
        SalesOrder order = new SalesOrder(
                customer,
                orderNumber,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("95.00")
        );
        order.addItem(product, "SKU-001", "Operations Keyboard", new BigDecimal("100.00"), 1);
        return salesOrderRepository.saveAndFlush(order);
    }
}
