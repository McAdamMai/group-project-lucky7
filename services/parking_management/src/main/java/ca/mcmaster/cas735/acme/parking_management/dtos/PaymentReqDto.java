package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PaymentReqDto {
    private String macID;
    private String licensePlate;
    private Integer bill;
    private Integer timeStamp;
    private String paymentMethod;
}
//{"macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "bank"}
