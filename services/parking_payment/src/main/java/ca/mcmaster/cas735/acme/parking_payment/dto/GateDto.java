package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GateDto {
    private String licensePlate;
    private Integer bill;
    private Integer timeStamp;
    private String fineReason;
}

//{"licensePlate": "E57U1", "bill":10, "timeStamp": 1731425099, "fineReason": "None"}