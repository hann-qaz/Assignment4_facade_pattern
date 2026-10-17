package kz.edu.facade.model;

import java.math.BigDecimal;

public record CartItem(String productId, int quantity, BigDecimal unitPrice) {

    public CartItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product id cannot be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

