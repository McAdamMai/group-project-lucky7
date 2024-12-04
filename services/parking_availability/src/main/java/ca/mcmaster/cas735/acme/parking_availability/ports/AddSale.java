package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.InitializationRequest;

public interface AddSale {

    void request2Payment(InitializationRequest initializationRequest);
    void request2Management(InitializationRequest initializationRequest);
}