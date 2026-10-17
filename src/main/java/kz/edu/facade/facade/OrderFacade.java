package kz.edu.facade.facade;

import kz.edu.facade.model.Cart;
import kz.edu.facade.model.Customer;
import kz.edu.facade.model.Order;
import kz.edu.facade.model.OrderResult;
import kz.edu.facade.model.OrderStatus;
import kz.edu.facade.subsystem.InventoryException;
import kz.edu.facade.subsystem.InventoryService;
import kz.edu.facade.subsystem.NotificationService;
import kz.edu.facade.subsystem.OrderRepository;
import kz.edu.facade.subsystem.PaymentException;
import kz.edu.facade.subsystem.PaymentProcessor;
import kz.edu.facade.subsystem.ShippingException;
import kz.edu.facade.subsystem.ShippingService;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class OrderFacade {
    private final InventoryService inventoryService;
    private final PaymentProcessor paymentProcessor;
    private final ShippingService shippingService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final Supplier<String> orderIdSupplier;

    public OrderFacade(
            InventoryService inventoryService,
            PaymentProcessor paymentProcessor,
            ShippingService shippingService,
            NotificationService notificationService,
            OrderRepository orderRepository
    ) {
        this(
                inventoryService,
                paymentProcessor,
                shippingService,
                notificationService,
                orderRepository,
                () -> "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
    }

    OrderFacade(
            InventoryService inventoryService,
            PaymentProcessor paymentProcessor,
            ShippingService shippingService,
            NotificationService notificationService,
            OrderRepository orderRepository,
            Supplier<String> orderIdSupplier
    ) {
        this.inventoryService = Objects.requireNonNull(inventoryService);
        this.paymentProcessor = Objects.requireNonNull(paymentProcessor);
        this.shippingService = Objects.requireNonNull(shippingService);
        this.notificationService = Objects.requireNonNull(notificationService);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.orderIdSupplier = Objects.requireNonNull(orderIdSupplier);
    }

    public OrderResult placeOrder(Cart cart, Customer customer) {
        Objects.requireNonNull(cart, "Cart cannot be null");
        Objects.requireNonNull(customer, "Customer cannot be null");

        String orderId = orderIdSupplier.get();

        try {
            inventoryService.reserve(orderId, cart.items());
        } catch (InventoryException exception) {
            return failOrder(customer, orderId, exception.getMessage());
        }

        String paymentId;
        try {
            paymentId = paymentProcessor.charge(orderId, customer.paymentToken(), cart.total());
        } catch (PaymentException exception) {
            inventoryService.release(orderId);
            return failOrder(customer, orderId, exception.getMessage());
        }

        String shipmentId;
        try {
            shipmentId = shippingService.scheduleDelivery(orderId, customer.shippingAddress());
        } catch (ShippingException exception) {
            paymentProcessor.refund(paymentId);
            inventoryService.release(orderId);
            return failOrder(customer, orderId, exception.getMessage());
        }

        Order order = new Order(
                orderId,
                cart,
                customer,
                paymentId,
                shipmentId,
                OrderStatus.CONFIRMED
        );
        orderRepository.save(order);
        notificationService.sendOrderConfirmed(customer.email(), orderId);
        return OrderResult.success(orderId, "Order placed successfully");
    }

    public OrderResult cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return OrderResult.failure(orderId, "Order not found");
        }
        if (order.status() == OrderStatus.CANCELLED) {
            return OrderResult.failure(orderId, "Order is already cancelled");
        }

        shippingService.cancelShipment(order.shipmentId());
        paymentProcessor.refund(order.paymentId());
        inventoryService.release(orderId);
        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED);
        notificationService.sendOrderCancelled(order.customer().email(), orderId);

        return OrderResult.success(orderId, "Order cancelled successfully");
    }

    private OrderResult failOrder(Customer customer, String orderId, String reason) {
        notificationService.sendOrderFailed(customer.email(), orderId, reason);
        return OrderResult.failure(orderId, reason);
    }
}

