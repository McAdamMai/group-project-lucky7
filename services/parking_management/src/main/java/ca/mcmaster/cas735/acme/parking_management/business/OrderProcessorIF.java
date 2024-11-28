package ca.mcmaster.cas735.acme.parking_management.business;

import ca.mcmaster.cas735.acme.parking_management.dtos.OrderReqDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.OrderResDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.Payment2ManagementDto;

public interface OrderProcessorIF {
    OrderResDto processRegistration(OrderReqDto orderReqDto);
    OrderResDto processRenewal(OrderReqDto orderReqDto);
    OrderResDto processDeletion(OrderReqDto orderReqDto);
    void processPayment(Payment2ManagementDto payment2ManagementDto);
}
