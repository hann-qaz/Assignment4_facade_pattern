package kz.edu.facade.subsystem;

import kz.edu.facade.model.CartItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        Map<String, Integer> requiredQuantity = items.stream()
                .collect(Collectors.groupingBy(
                        CartItem::productId,
                        Collectors.summingInt(CartItem::quantity)
                ));

        for (Map.Entry<String, Integer> entry : requiredQuantity.entrySet()) {
            if (availableQuantity(entry.getKey()) < entry.getValue()) {
                throw new InventoryException("Not enough stock for " + entry.getKey());
            }
        }

        requiredQuantity.forEach((productId, quantity) ->
                stock.compute(productId, (key, value) -> value - quantity)
        );
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

