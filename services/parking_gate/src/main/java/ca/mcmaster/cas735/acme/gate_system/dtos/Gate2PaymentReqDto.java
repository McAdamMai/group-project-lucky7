package ca.mcmaster.cas735.acme.gate_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Gate2PaymentReqDto {
    private String licensePlate;
    private Integer bill;
    private Long timeStamp; //use to generate uuid
    private String gateID; //locate the specific pos attached to gate
}

//{"licensePlate": "E57U1", "bill":10, "timeStamp": 1731425099, "gateID": "Gate10"}
