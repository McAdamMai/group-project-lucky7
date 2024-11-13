package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManagerDto {
    private String macID;
    private Integer bill;
    private Integer timeStamp;
    private String paymentMethod;
}

//{"macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "bank"}
//{"macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "mac"}
//{"macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "others"}