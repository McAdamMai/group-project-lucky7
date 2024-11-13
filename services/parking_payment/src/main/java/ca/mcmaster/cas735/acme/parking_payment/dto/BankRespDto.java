package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BankRespDto {
    private String info;
    private Boolean ack; // set to true
}

//{"info": "400608194", "ack": true}
//{"info": "400608194", "ack": false}
