package ca.mcmaster.cas735.acme.parking_enforcement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FinePaymentDTO {
    String macID;
    String licensePlate;
    Integer bill;
    Integer timeStamp;
    String fineReason;
}
