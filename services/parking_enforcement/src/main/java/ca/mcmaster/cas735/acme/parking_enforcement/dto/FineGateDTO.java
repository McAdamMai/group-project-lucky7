package ca.mcmaster.cas735.acme.parking_enforcement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FineGateDTO {
    private String licensePlate;
    private Integer bill;
    private Long timeStamp;
    private String fineReason;
}
