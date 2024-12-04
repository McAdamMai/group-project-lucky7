package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.GateCheckSpaceDto;

public interface GateReq {
    boolean checkSpace(GateCheckSpaceDto gateCheckSpaceDto);
}
