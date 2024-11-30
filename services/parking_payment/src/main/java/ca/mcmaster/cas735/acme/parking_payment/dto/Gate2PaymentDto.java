package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Gate2PaymentDto {
    private String licensePlate;
    private Integer bill;
    private Long timeStamp; //use to generate uuid
    private String gateID; //locate the specific pos attached to gate
}

//{"licensePlate": "E57U1", "bill":10, "timeStamp": 1731425099, "gateID": "Gate10"}