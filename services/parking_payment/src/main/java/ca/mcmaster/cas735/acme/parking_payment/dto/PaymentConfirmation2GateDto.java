package ca.mcmaster.cas735.acme.parking_payment.dto;

import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfPaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PaymentConfirmation2GateDto {
    private String licensePlate;
    private TypeOfPaymentStatus paymentStatus;
}
