package ca.mcmaster.cas735.acme.gate_system.ports;

import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2AvailabilityResDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PaymentReqDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PermitReqDto;

public interface GateIF {
    void openGate(String gate);
    void generateQRCode(Long QRCode);
    void sendValidationRequest(Gate2PermitReqDto gate2PermitReqDto);
    void sendAvailabilities(Gate2AvailabilityResDto gate2AvailabilityResDto);
    void sendPaymentRequest(Gate2PaymentReqDto gate2PaymentReqDto);
}
