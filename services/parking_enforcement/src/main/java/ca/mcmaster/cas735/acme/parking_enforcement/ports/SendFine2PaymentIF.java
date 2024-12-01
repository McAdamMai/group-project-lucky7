package ca.mcmaster.cas735.acme.parking_enforcement.ports;

import ca.mcmaster.cas735.acme.parking_enforcement.dto.FinePaymentDTO;

public interface SendFine2PaymentIF {
    void sendFinePayment(FinePaymentDTO fine);
}
