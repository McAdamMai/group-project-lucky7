package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.MonitorRequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.RequestDTO;

public interface Monitor {

    void send2monitor(MonitorRequestDTO monitorRequestDTO);

}