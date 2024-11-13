package ca.mcmaster.cas735.acme.parking_payment.ports;

import ca.mcmaster.cas735.acme.parking_payment.dto.ManagerDto;

public interface ReqToBankIF {
    void sendPaymentRequest(String info,Integer bill);
}
