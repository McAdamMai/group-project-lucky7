package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentID2PosDto { //change to the pos
    private String PaymentID;
    private String gateID;
}
