package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import ca.mcmaster.cas735.acme.parking_management.utils.TypeOfPaymentStatus;

@NoArgsConstructor
@Data
public class Payment2ManagementDto {
    private String macID;
    private TypeOfPaymentStatus paymentStatus;
}
//{"licensePlate":"U57U1","macID":"400608194","timeStamp":1731425099,"paymentStatus":true}
