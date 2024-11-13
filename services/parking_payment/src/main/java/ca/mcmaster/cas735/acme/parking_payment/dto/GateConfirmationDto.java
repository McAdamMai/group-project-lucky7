package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GateConfirmationDto {
    private String licensePlate;
    private Boolean paymentStatus;
}
