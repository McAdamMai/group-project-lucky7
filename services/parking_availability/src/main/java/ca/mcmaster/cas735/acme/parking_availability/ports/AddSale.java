package ca.mcmaster.cas735.acme.parking_availability.ports;

import ca.mcmaster.cas735.acme.parking_availability.dto.BillDTO;

public interface AddSale {

    void addSale(BillDTO bill);

}