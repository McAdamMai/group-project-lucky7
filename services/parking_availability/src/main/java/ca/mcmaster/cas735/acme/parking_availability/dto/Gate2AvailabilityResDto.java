package ca.mcmaster.cas735.acme.parking_availability.dto;

import ca.mcmaster.cas735.acme.parking_availability.utils.TypeOfClient;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Gate2AvailabilityResDto {
    private Boolean isEnter;
    private String licensePlate;
    private TypeOfClient typeOfClient;
    private Long time;
    private String gate;
}
// {"isEnter": true, "permit": "visitor", "time": 1733034715319, "license":"ABCDE", "gate":"EXIT12345"}