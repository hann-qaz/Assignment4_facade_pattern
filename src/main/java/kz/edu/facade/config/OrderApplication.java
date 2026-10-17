package kz.edu.facade.config;

import kz.edu.facade.facade.OrderFacade;
import kz.edu.facade.subsystem.InventoryService;
import kz.edu.facade.subsystem.NotificationService;
import kz.edu.facade.subsystem.OrderRepository;
import kz.edu.facade.subsystem.PaymentProcessor;
import kz.edu.facade.subsystem.ShippingService;

public final class OrderApplication {
    private OrderApplication() {
    }

    public static OrderFacade createFacade() {
        InventoryService inventoryService = new InventoryService();
        inventoryService.addStock("LAPTOP-STAND", 10);
        inventoryService.addStock("USB-C-CABLE", 25);

        return new OrderFacade(
                inventoryService,
                new PaymentProcessor(),
                new ShippingService(),
                new NotificationService(),
                new OrderRepository()
        );
    }
}

