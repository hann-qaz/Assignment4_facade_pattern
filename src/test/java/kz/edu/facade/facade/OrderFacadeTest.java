package kz.edu.facade.facade;

import kz.edu.facade.model.Cart;
import kz.edu.facade.model.CartItem;
import kz.edu.facade.model.Customer;
import kz.edu.facade.model.OrderResult;
import kz.edu.facade.model.OrderStatus;
import kz.edu.facade.subsystem.InventoryService;
import kz.edu.facade.subsystem.NotificationService;
import kz.edu.facade.subsystem.OrderRepository;
import kz.edu.facade.subsystem.PaymentProcessor;
import kz.edu.facade.subsystem.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderFacadeTest {
    private InventoryService inventoryService;
    private PaymentProcessor paymentProcessor;
    private ShippingService shippingService;
    private NotificationService notificationService;
    private OrderRepository orderRepository;
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
        inventoryService.addStock("BOOK", 5);
        paymentProcessor = new PaymentProcessor();
        shippingService = new ShippingService();
        notificationService = new NotificationService();
        orderRepository = new OrderRepository();

        orderFacade = new OrderFacade(
                inventoryService,
                paymentProcessor,
                shippingService,
                notificationService,
                orderRepository,
                () -> "ORD-TEST-1"
        );
    }

    @Test
    void placesOrderThroughOneFacadeCall() {
        OrderResult result = orderFacade.placeOrder(cart(), validCustomer());

        assertTrue(result.success());
        assertEquals(3, inventoryService.availableQuantity("BOOK"));
        assertTrue(paymentProcessor.wasCharged("PAY-ORD-TEST-1"));
        assertTrue(shippingService.isScheduled("SHIP-ORD-TEST-1"));
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(result.orderId()).orElseThrow().status());
        assertTrue(notificationService.sentMessages().get(0).contains("confirmed"));
    }

    @Test
    void releasesStockWhenPaymentIsDeclined() {
        Customer declinedCustomer = new Customer(
                "Student",
                "student@example.com",
                "DECLINED",
                "Almaty"
        );

        OrderResult result = orderFacade.placeOrder(cart(), declinedCustomer);

        assertFalse(result.success());
        assertEquals(5, inventoryService.availableQuantity("BOOK"));
        assertFalse(paymentProcessor.wasCharged("PAY-ORD-TEST-1"));
        assertFalse(shippingService.isScheduled("SHIP-ORD-TEST-1"));
        assertTrue(notificationService.sentMessages().get(0).contains("declined"));
    }

    @Test
    void refundsPaymentAndReleasesStockWhenShippingFails() {
        Customer unreachableCustomer = new Customer(
                "Student",
                "student@example.com",
                "VALID-TOKEN",
                "UNSERVICEABLE AREA"
        );

        OrderResult result = orderFacade.placeOrder(cart(), unreachableCustomer);

        assertFalse(result.success());
        assertEquals(5, inventoryService.availableQuantity("BOOK"));
        assertTrue(paymentProcessor.wasRefunded("PAY-ORD-TEST-1"));
        assertFalse(shippingService.isScheduled("SHIP-ORD-TEST-1"));
    }

    @Test
    void stopsBeforePaymentWhenStockIsInsufficient() {
        Cart tooLargeCart = new Cart(List.of(
                new CartItem("BOOK", 10, new BigDecimal("5000"))
        ));

        OrderResult result = orderFacade.placeOrder(tooLargeCart, validCustomer());

        assertFalse(result.success());
        assertEquals(5, inventoryService.availableQuantity("BOOK"));
        assertFalse(paymentProcessor.wasCharged("PAY-ORD-TEST-1"));
    }

    @Test
    void countsRepeatedProductsBeforeReservingStock() {
        Cart cartWithRepeatedProduct = new Cart(List.of(
                new CartItem("BOOK", 3, new BigDecimal("5000")),
                new CartItem("BOOK", 3, new BigDecimal("5000"))
        ));

        OrderResult result = orderFacade.placeOrder(cartWithRepeatedProduct, validCustomer());

        assertFalse(result.success());
        assertEquals(5, inventoryService.availableQuantity("BOOK"));
    }

    @Test
    void cancelsConfirmedOrderAndRestoresResources() {
        OrderResult placedOrder = orderFacade.placeOrder(cart(), validCustomer());

        OrderResult cancelledOrder = orderFacade.cancelOrder(placedOrder.orderId());

        assertTrue(cancelledOrder.success());
        assertEquals(5, inventoryService.availableQuantity("BOOK"));
        assertTrue(paymentProcessor.wasRefunded("PAY-ORD-TEST-1"));
        assertFalse(shippingService.isScheduled("SHIP-ORD-TEST-1"));
        assertEquals(OrderStatus.CANCELLED, orderRepository.findById(placedOrder.orderId()).orElseThrow().status());
    }

    @Test
    void rejectsSecondCancellation() {
        OrderResult placedOrder = orderFacade.placeOrder(cart(), validCustomer());
        orderFacade.cancelOrder(placedOrder.orderId());

        OrderResult secondCancellation = orderFacade.cancelOrder(placedOrder.orderId());

        assertFalse(secondCancellation.success());
        assertEquals("Order is already cancelled", secondCancellation.message());
    }

    private Cart cart() {
        return new Cart(List.of(
                new CartItem("BOOK", 2, new BigDecimal("5000"))
        ));
    }

    private Customer validCustomer() {
        return new Customer(
                "Student",
                "student@example.com",
                "VALID-TOKEN",
                "Almaty"
        );
    }
}

