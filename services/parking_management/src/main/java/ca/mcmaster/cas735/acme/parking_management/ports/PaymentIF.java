package ca.mcmaster.cas735.acme.parking_management.ports;

import ca.mcmaster.cas735.acme.parking_management.dtos.Management2PaymentDto;

public interface PaymentIF {
    void sendToPayment(Management2PaymentDto management2PaymentDto);
}
