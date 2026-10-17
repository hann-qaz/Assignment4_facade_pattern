package kz.edu.facade.model;

public record Customer(String name, String email, String paymentToken, String shippingAddress) {

    public Customer {
        requireText(name, "Customer name");
        requireText(email, "Email");
        requireText(paymentToken, "Payment token");
        requireText(shippingAddress, "Shipping address");
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
    }
}

