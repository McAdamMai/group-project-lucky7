package ca.mcmaster.cas735.acme.parking_enforcement.ports;

import ca.mcmaster.cas735.acme.parking_enforcement.dto.FinePaymentDTO;

public interface FineFilter {

    public void sendFinePayment(FinePaymentDTO fine);

}