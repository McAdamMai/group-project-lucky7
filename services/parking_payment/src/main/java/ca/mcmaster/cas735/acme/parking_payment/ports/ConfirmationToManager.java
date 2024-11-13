package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.ManagerConfirmationDto;

public interface ConfirmationToManager {
    void sendConfirmationToManager(ManagerConfirmationDto managerConfirmationDto);
}
