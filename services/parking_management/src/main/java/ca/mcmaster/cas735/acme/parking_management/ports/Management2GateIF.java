package ca.mcmaster.cas735.acme.parking_management.ports;

import ca.mcmaster.cas735.acme.parking_management.dtos.Permit2GateResDto;

public interface Management2GateIF {
    void update2gate(Permit2GateResDto permit2GateResDto);
}
