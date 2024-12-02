package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RequestDTO {
    private Boolean isEnter;
    private String permit;
    private Long time;
    private String license;
    private String gate;
}
// {"isEnter": true, "permit": "visitor", "time": 1733034715319, "license":"ABCDE", "gate":"EXIT12345"}