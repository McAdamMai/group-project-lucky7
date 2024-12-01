package ca.mcmaster.cas735.acme.parking_enforcement.ports;

import ca.mcmaster.cas735.acme.parking_enforcement.dto.FineGateDTO;

public interface SendFine2GateIF {
    void sendFineGate(FineGateDTO fine);
}