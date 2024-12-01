package ca.mcmaster.cas735.acme.parking_management.ports;

import ca.mcmaster.cas735.acme.parking_management.dtos.PaymentRespDto;

public interface TransponderOperationIF {
    void createTransponder(PaymentRespDto paymentRespDto);
}
