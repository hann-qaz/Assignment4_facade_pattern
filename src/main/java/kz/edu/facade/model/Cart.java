package kz.edu.facade.model;

import java.math.BigDecimal;
import java.util.List;

public record Cart(List<CartItem> items) {

    public Cart {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cart must contain at least one item");
        }
        items = List.copyOf(items);
    }

    public BigDecimal total() {
        return items.stream()
                .map(CartItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

