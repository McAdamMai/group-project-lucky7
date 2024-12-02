package ca.mcmaster.cas735.acme.parking_management.ports;

import ca.mcmaster.cas735.acme.parking_management.dtos.AvailabilityResp;

public interface Management2avlIF {
    void send2val(AvailabilityResp availabilityResp);
}
