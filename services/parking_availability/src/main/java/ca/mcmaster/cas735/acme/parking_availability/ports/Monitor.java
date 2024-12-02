package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.MonitorRequestDTO;

public interface Monitor {

    void monitor(MonitorRequestDTO request);

}