package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentConfirmation2ManagementDto;

public interface PaymentConfirmation2ManagementIF {
    void sendConfirmationToManager(PaymentConfirmation2ManagementDto paymentConfirmation2ManagementDto);
}
