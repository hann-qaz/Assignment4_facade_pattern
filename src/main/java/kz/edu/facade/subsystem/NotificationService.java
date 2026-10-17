package kz.edu.facade.subsystem;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final List<String> sentMessages = new ArrayList<>();

    public void sendOrderConfirmed(String email, String orderId) {
        send(email, "Order " + orderId + " is confirmed");
    }

    public void sendOrderCancelled(String email, String orderId) {
        send(email, "Order " + orderId + " is cancelled");
    }

    public void sendOrderFailed(String email, String orderId, String reason) {
        send(email, "Order " + orderId + " failed: " + reason);
    }

    private void send(String email, String message) {
        sentMessages.add(email + ": " + message);
    }

    public List<String> sentMessages() {
        return List.copyOf(sentMessages);
    }
}

