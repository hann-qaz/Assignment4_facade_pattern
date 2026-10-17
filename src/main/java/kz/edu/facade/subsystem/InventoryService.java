package kz.edu.facade.subsystem;

import kz.edu.facade.model.CartItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, List<CartItem>> reservations = new HashMap<>();

    public void addStock(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        stock.merge(productId, quantity, Integer::sum);
    }

    public void reserve(String orderId, List<CartItem> items) {
        if (reservations.containsKey(orderId)) {
            throw new InventoryException("Stock is already reserved for " + orderId);
        }

        for (CartItem item : items) {
            int available = availableQuantity(item.productId());
            if (available < item.quantity()) {
                throw new InventoryException("Not enough stock for " + item.productId());
            }
        }

        for (CartItem item : items) {
            stock.compute(item.productId(), (key, value) -> value - item.quantity());
        }
        reservations.put(orderId, List.copyOf(items));
    }

    public boolean release(String orderId) {
        List<CartItem> items = reservations.remove(orderId);
        if (items == null) {
            return false;
        }

        for (CartItem item : items) {
            stock.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return true;
    }

    public int availableQuantity(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    public boolean hasReservation(String orderId) {
        return reservations.containsKey(orderId);
    }
}

