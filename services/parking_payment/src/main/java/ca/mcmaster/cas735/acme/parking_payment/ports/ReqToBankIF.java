package ca.mcmaster.cas735.acme.parking_payment.ports;

public interface ReqToBankIF {
    void sendPaymentRequest(String info,Integer bill);
}
