package kz.edu.facade.model;

public record OrderResult(boolean success, String orderId, String message) {

    public static OrderResult success(String orderId, String message) {
        return new OrderResult(true, orderId, message);
    }

    public static OrderResult failure(String orderId, String message) {
        return new OrderResult(false, orderId, message);
    }
}

