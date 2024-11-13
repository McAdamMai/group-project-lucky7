package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.GateConfirmationDto;

public interface ConfirmationToGate {
    void sendConfirmationToGate(GateConfirmationDto gateConfirmationDto);
}
