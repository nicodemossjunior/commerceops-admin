package com.commerceops.admin.audit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.commerceops.admin.audit.model.AuditAction;
import com.commerceops.admin.audit.service.AuditRecorder;
import com.commerceops.admin.auth.dto.LoginRequest;
import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.repository.AdminUserRepository;
import com.commerceops.admin.auth.service.AuthService;
import com.commerceops.admin.auth.service.AuthenticationFailedException;
import com.commerceops.admin.catalog.dto.ProductRequest;
import com.commerceops.admin.catalog.model.Category;
import com.commerceops.admin.catalog.model.CategoryStatus;
import com.commerceops.admin.catalog.model.Product;
import com.commerceops.admin.catalog.model.ProductStatus;
import com.commerceops.admin.catalog.repository.CategoryRepository;
import com.commerceops.admin.catalog.repository.ProductRepository;
import com.commerceops.admin.catalog.service.ProductService;
import com.commerceops.admin.common.security.CurrentUser;
import com.commerceops.admin.common.security.CurrentUserProvider;
import com.commerceops.admin.customers.model.Customer;
import com.commerceops.admin.customers.model.CustomerStatus;
import com.commerceops.admin.customers.repository.CustomerRepository;
import com.commerceops.admin.orders.model.SalesOrder;
import com.commerceops.admin.orders.repository.SalesOrderRepository;
import com.commerceops.admin.orders.service.OrderService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DomainAuditIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuditRecorder auditRecorder;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    void productCreationEmitsDomainAuditAction() {
        Category category = categoryRepository.saveAndFlush(
                new Category("Audit Products", "audit-products", null, CategoryStatus.ACTIVE, null)
        );

        var response = productService.create(new ProductRequest(
                category.getPublicId(),
                "AUDIT-SKU",
                "Audited Product",
                "audited-product",
                null,
                new BigDecimal("49.90"),
                null,
                8,
                ProductStatus.ACTIVE
        ));

        verify(auditRecorder).record(
                eq(AuditAction.PRODUCT_CREATED),
                eq("PRODUCT"),
                eq(response.publicId()),
                eq(Map.of("sku", "AUDIT-SKU", "status", "ACTIVE"))
        );
    }

    @Test
    void orderTransitionEmitsSpecializedAuditAction() {
        SalesOrder order = saveOrder();
        AdminUser actor = adminUserRepository.saveAndFlush(new AdminUser(
                "Audit Order Manager",
                "audit.order.manager@example.com",
                "test-password-hash",
                Set.of()
        ));
        when(currentUserProvider.currentUser()).thenReturn(
                new CurrentUser(actor.getId(), actor.getPublicId(), actor.getEmail(), Set.of("MANAGER"))
        );

        orderService.cancel(order.getPublicId(), "Customer requested cancellation.");

        verify(auditRecorder).record(
                eq(AuditAction.ORDER_CANCELLED),
                eq("ORDER"),
                eq(order.getPublicId()),
                any(Map.class)
        );
    }

    @Test
    void successfulAndFailedLoginsEmitAuditActionsWithoutCredentials() {
        AdminUser user = adminUserRepository.saveAndFlush(new AdminUser(
                "Audit Login User",
                "audit.login@example.com",
                passwordEncoder.encode("valid-password"),
                Set.of()
        ));

        authService.login(new LoginRequest(user.getEmail(), "valid-password"));
        assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "wrong-password")))
                .isInstanceOf(AuthenticationFailedException.class);

        verify(auditRecorder).recordAs(
                eq(user.getId()),
                eq(user.getEmail()),
                eq(AuditAction.AUTH_LOGIN_SUCCESS),
                eq("ADMIN_USER"),
                eq(user.getPublicId()),
                eq(Map.of("result", "SUCCESS"))
        );
        verify(auditRecorder).recordAs(
                eq(user.getId()),
                eq(user.getEmail()),
                eq(AuditAction.AUTH_LOGIN_FAILURE),
                eq("ADMIN_USER"),
                eq(user.getPublicId()),
                eq(Map.of("result", "INVALID_CREDENTIALS"))
        );
    }

    private SalesOrder saveOrder() {
        Customer customer = customerRepository.saveAndFlush(
                new Customer("Audit Customer", "audit.customer@example.com", null, null, CustomerStatus.ACTIVE)
        );
        Category category = categoryRepository.saveAndFlush(
                new Category("Audit Orders", "audit-orders", null, CategoryStatus.ACTIVE, null)
        );
        Product product = productRepository.saveAndFlush(
                new Product(category, "AUDIT-ORDER-SKU", "Audit Order Product", "audit-order-product", null,
                        new BigDecimal("75.00"), null, 20, ProductStatus.ACTIVE)
        );
        BigDecimal amount = new BigDecimal("75.00");
        SalesOrder order = new SalesOrder(customer, "AUDIT-ORDER-001", amount, BigDecimal.ZERO, BigDecimal.ZERO, amount);
        order.addItem(product, product.getSku(), product.getName(), amount, 1);
        return salesOrderRepository.saveAndFlush(order);
    }
}
