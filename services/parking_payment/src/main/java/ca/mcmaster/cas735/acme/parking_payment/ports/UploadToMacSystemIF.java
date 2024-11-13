package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.EnforcementDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.ManagerDto;

public interface UploadToMacSystemIF {
    void updateFine(EnforcementDto enforcementDto);
    void updateTransponder(ManagerDto managerDto);
}
