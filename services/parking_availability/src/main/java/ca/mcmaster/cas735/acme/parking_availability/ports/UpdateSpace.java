package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.Gate2AvailabilityResDto;

public interface UpdateSpace {
    void updateSpace(Gate2AvailabilityResDto request);
}
