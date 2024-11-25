package ca.mcmaster.cas735.acme.gate_system.ports;

import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PaymentReqDto;

public interface PaymentLaunchingIF {
    void launchPaymentMsgBus(Gate2PaymentReqDto gate2PaymentReqDto);
}
