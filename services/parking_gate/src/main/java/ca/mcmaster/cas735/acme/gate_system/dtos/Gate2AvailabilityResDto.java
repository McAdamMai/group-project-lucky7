package ca.mcmaster.cas735.acme.gate_system.dtos;

import ca.mcmaster.cas735.acme.gate_system.utils.TypeOfClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Gate2AvailabilityResDto {
    private String licensePlate;
    private TypeOfClient typeOfClient;
    private Long entryTime;
    private Long exitTime;
}
