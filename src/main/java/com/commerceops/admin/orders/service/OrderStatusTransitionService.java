package com.commerceops.admin.orders.service;

import com.commerceops.admin.common.error.BusinessRuleException;
import com.commerceops.admin.orders.model.OrderStatus;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusTransitionService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = allowedTransitions();

    public void validate(OrderStatus currentStatus, OrderStatus requestedStatus) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(requestedStatus)) {
            throw new BusinessRuleException(
                    "Order status cannot transition from %s to %s."
                            .formatted(currentStatus, requestedStatus)
            );
        }
    }

    private static Map<OrderStatus, Set<OrderStatus>> allowedTransitions() {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PAID,
                Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED, OrderStatus.REFUNDED));
        transitions.put(OrderStatus.PROCESSING,
                Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED, OrderStatus.REFUNDED));
        transitions.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.REFUNDED));
        transitions.put(OrderStatus.DELIVERED, Set.of(OrderStatus.REFUNDED));
        transitions.put(OrderStatus.CANCELLED, Set.of());
        transitions.put(OrderStatus.REFUNDED, Set.of());
        return Map.copyOf(transitions);
    }
}
