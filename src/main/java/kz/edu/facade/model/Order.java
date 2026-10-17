package kz.edu.facade.model;

public record Order(
        String id,
        Cart cart,
        Customer customer,
        String paymentId,
        String shipmentId,
        OrderStatus status
) {
    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, cart, customer, paymentId, shipmentId, newStatus);
    }
}

