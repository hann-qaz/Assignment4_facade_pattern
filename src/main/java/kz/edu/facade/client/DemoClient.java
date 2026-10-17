package kz.edu.facade.client;

import kz.edu.facade.config.OrderApplication;
import kz.edu.facade.facade.OrderFacade;
import kz.edu.facade.model.Cart;
import kz.edu.facade.model.CartItem;
import kz.edu.facade.model.Customer;
import kz.edu.facade.model.OrderResult;

import java.math.BigDecimal;
import java.util.List;

public class DemoClient {
    public static void main(String[] args) {
        OrderFacade orderFacade = OrderApplication.createFacade();

        Cart cart = new Cart(List.of(
                new CartItem("LAPTOP-STAND", 1, new BigDecimal("12990")),
                new CartItem("USB-C-CABLE", 2, new BigDecimal("3990"))
        ));
        Customer customer = new Customer(
                "Amirkhan Tulkibayev",
                "amirkhan@example.com",
                "VALID-TOKEN",
                "Almaty, Abay Avenue 1"
        );

        OrderResult placedOrder = orderFacade.placeOrder(cart, customer);
        System.out.println(placedOrder.message() + ": " + placedOrder.orderId());

        OrderResult cancelledOrder = orderFacade.cancelOrder(placedOrder.orderId());
        System.out.println(cancelledOrder.message() + ": " + cancelledOrder.orderId());
    }
}

