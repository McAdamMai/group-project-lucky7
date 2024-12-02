package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.Payment2AvailDTO;

public interface Payment2Avl {
    void send2avl(Payment2AvailDTO payment2AvlDto);
}
