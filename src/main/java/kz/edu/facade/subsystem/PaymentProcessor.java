package kz.edu.facade.subsystem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {
    private final Map<String, PaymentRecord> payments = new HashMap<>();

    public String charge(String orderId, String paymentToken, BigDecimal amount) {
        if ("DECLINED".equalsIgnoreCase(paymentToken)) {
            throw new PaymentException("Payment was declined");
        }
        if (amount.signum() <= 0) {
            throw new PaymentException("Payment amount must be positive");
        }

        String paymentId = "PAY-" + orderId;
        payments.put(paymentId, new PaymentRecord(amount, false));
        return paymentId;
    }

    public boolean refund(String paymentId) {
        PaymentRecord payment = payments.get(paymentId);
        if (payment == null || payment.refunded()) {
            return false;
        }
        payments.put(paymentId, new PaymentRecord(payment.amount(), true));
        return true;
    }

    public boolean wasCharged(String paymentId) {
        return payments.containsKey(paymentId);
    }

    public boolean wasRefunded(String paymentId) {
        PaymentRecord payment = payments.get(paymentId);
        return payment != null && payment.refunded();
    }

    private record PaymentRecord(BigDecimal amount, boolean refunded) {
    }
}

