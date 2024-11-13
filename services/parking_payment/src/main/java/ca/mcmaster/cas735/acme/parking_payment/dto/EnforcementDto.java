package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnforcementDto { // all attributes should be at lower case
    private String macID;
    private String licensePlate;
    private Integer bill;
    private Integer timeStamp;
    private String fineReason;
}
//{"macID": "400608194", "licensePlate":"A57U1", "bill":10, "timeStamp": 1731425099, "fineReason": "Sigma"}