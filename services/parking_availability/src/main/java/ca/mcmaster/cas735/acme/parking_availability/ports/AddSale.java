package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.AvailabilityRequest;
import ca.mcmaster.cas735.acme.parking_availability.dto.Management2AvailDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.Payment2AvailDTO;

public interface AddSale {

    void request2Payment(AvailabilityRequest availabilityRequest);
    void request2Management(AvailabilityRequest availabilityRequest);
}