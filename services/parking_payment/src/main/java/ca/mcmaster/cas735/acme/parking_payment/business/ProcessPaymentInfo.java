package ca.mcmaster.cas735.acme.parking_payment.business;

import ca.mcmaster.cas735.acme.parking_payment.dto.Bank2PaymentDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.Gate2PaymentDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.Management2PaymentDto;

public interface ProcessPaymentInfo {
    void processPaymentFromGate(Gate2PaymentDto gate2PaymentDto);
    void processPaymentFromManagement(Management2PaymentDto management2PaymentDto);
    void processConfirmationFromBank(Bank2PaymentDto bank2PaymentDto);
}
