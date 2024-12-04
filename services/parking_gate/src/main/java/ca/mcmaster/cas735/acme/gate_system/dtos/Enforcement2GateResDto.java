package ca.mcmaster.cas735.acme.gate_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Enforcement2GateResDto {
    private String licensePlate;
    private Integer bill;
    private Long timeStamp;
    private String fineReason;
}
