package ca.mcmaster.cas735.acme.gate_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Availability2GateResDto {
    private Boolean isAvailable;
    private String gate;
}
