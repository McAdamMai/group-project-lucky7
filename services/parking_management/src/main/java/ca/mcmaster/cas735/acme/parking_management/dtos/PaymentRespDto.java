package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PaymentRespDto {
    private String macID;
    private String licensePlate;
    private Integer timeStamp;
    private Boolean paymentStatus;
}
//{"licensePlate":"U57U1","macID":"400608194","timeStamp":1731425099,"paymentStatus":true}
