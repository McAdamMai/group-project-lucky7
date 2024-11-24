package ca.mcmaster.cas735.acme.gate_system.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Payment2GateResDto {
    private String licensePlate;
    private Boolean paymentStatus;
}
