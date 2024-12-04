package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GateCheckSpaceDto {
    private String gate;
    private Boolean isEnter;
}
