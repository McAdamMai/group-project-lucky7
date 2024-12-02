package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.RequestDTO;

public interface CheckSpace {

    void checkSpace(RequestDTO request);

}