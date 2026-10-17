package kz.edu.facade.subsystem;

import kz.edu.facade.model.Order;
import kz.edu.facade.model.OrderStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OrderRepository {
    private final Map<String, Order> orders = new HashMap<>();

    public void save(Order order) {
        orders.put(order.id(), order);
    }

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public void updateStatus(String orderId, OrderStatus status) {
        Order currentOrder = findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        orders.put(orderId, currentOrder.withStatus(status));
    }
}

