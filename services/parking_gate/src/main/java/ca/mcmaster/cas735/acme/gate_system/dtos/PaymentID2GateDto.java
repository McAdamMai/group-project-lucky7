package ca.mcmaster.cas735.acme.gate_system.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentID2GateDto {
    private String PaymentID;
    private String licensePlate;
}
