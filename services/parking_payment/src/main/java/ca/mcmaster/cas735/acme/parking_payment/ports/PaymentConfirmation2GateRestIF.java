package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentConfirmation2GateDto;

public interface PaymentConfirmation2GateRestIF {
    void sendConfirmationToGateREST(PaymentConfirmation2GateDto paymentConfirmation2GateDto);
}
