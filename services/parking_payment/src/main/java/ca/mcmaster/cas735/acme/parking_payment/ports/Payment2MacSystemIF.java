package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.Enforcement2PaymentDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentConfirmation2ManagementDto;


public interface Payment2MacSystemIF {
    void updateFine(Enforcement2PaymentDto enforcement2PaymentDto);
    void updateTransponder(PaymentConfirmation2ManagementDto paymentConfirmation2ManagementDto);
}