package ca.mcmaster.cas735.acme.parking_management.ports;

import ca.mcmaster.cas735.acme.parking_management.dtos.Management2MacDto;

public interface Management2MacSystemIF {
    void updateTransponder(Management2MacDto management2PaymentDto);
}
