package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentConfirmation2GateDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentID2PosDto;

public interface PaymentConfirmation2GateMsgBusIF {
    void sendConfirmationToGate(PaymentConfirmation2GateDto paymentConfirmation2GateDto);
    void sendPaymentIDPos(PaymentID2PosDto paymentID2PosDto);
}
