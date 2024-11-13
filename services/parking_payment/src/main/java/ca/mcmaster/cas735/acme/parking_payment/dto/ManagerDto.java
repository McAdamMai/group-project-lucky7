package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManagerDto {
    private String licensePlate;
    private String macID;
    private Integer bill;
    private Integer timeStamp;
    private String paymentMethod;
}

//{"licensePlate": "E57U1","macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "bank"}
//{"licensePlate": "E57U1","macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "mac"}
//{"licensePlate": "E57U1","macID": "400608194", "bill":10, "timeStamp": 1731425099, "paymentMethod": "others"}