package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PaymentConfirmation2GateDto {
    private String licensePlate;
    private Integer paymentStatus;
    private String gateId;
}
