package ca.mcmaster.cas735.acme.parking_payment.dto;

import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfPaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Management2PaymentDto {
    private String macID;
    private Integer bill;
    private Long timestamp; // use to UUID
    private TypeOfPaymentMethod paymentMethod;
}

//{"licensePlate": "E57U1","macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "bank"}
//{"licensePlate": "E57U1","macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "mac"}
//{"licensePlate": "E57U1","macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "others"}