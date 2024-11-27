package ca.mcmaster.cas735.acme.parking_payment.dto;

import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfPaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentConfirmation2ManagementDto {
    private String macID;
    private TypeOfPaymentStatus paymentStatus;
}
