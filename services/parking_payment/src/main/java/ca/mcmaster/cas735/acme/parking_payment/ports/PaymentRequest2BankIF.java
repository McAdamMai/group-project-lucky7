package ca.mcmaster.cas735.acme.parking_payment.ports;

public interface PaymentRequest2BankIF {
    void sendPaymentRequest(String info,Integer bill);
}
