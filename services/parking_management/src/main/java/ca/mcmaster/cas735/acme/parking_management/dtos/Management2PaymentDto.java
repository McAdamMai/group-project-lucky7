package ca.mcmaster.cas735.acme.parking_management.dtos;

import ca.mcmaster.cas735.acme.parking_management.utils.TypeOfPaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Management2PaymentDto {
    private String macID;
    private Integer bill;
    private Long timestamp; // use to UUID
    private TypeOfPaymentMethod paymentMethod;
}
//{"macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "bank"}
