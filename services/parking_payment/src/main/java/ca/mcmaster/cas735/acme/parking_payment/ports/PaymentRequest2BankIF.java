package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentID2PosDto;

public interface PaymentRequest2BankIF {
    void sendPaymentRequest(String paymentId, Integer bill);
}
