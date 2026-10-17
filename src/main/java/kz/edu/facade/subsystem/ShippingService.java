package kz.edu.facade.subsystem;

import java.util.HashMap;
import java.util.Map;

public class ShippingService {
    private final Map<String, Boolean> shipments = new HashMap<>();

    public String scheduleDelivery(String orderId, String address) {
        if (address.toUpperCase().contains("UNSERVICEABLE")) {
            throw new ShippingException("The address is outside the delivery area");
        }

        String shipmentId = "SHIP-" + orderId;
        shipments.put(shipmentId, true);
        return shipmentId;
    }

    public boolean cancelShipment(String shipmentId) {
        if (!Boolean.TRUE.equals(shipments.get(shipmentId))) {
            return false;
        }
        shipments.put(shipmentId, false);
        return true;
    }

    public boolean isScheduled(String shipmentId) {
        return Boolean.TRUE.equals(shipments.get(shipmentId));
    }
}

