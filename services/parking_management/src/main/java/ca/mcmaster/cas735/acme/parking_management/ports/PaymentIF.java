package ca.mcmaster.cas735.acme.parking_management.ports;

import ca.mcmaster.cas735.acme.parking_management.dtos.PaymentReqDto;

public interface PaymentIF {
    void sendToPayment(PaymentReqDto paymentReqDto);
}
