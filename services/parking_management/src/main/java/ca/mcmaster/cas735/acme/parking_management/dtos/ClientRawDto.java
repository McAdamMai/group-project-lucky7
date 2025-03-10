package ca.mcmaster.cas735.acme.parking_management.dtos;

import ca.mcmaster.cas735.acme.parking_management.utils.TransponderType;
import ca.mcmaster.cas735.acme.parking_management.utils.TypeOfPaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ClientRawDto {
    private String macID;
    private String clientOrderId; // a 6 digit random numbers mix letters generated by client
    private String licensePlate;
    private TransponderType transponderType;
    private Long timeStamp;
    private TypeOfPaymentMethod paymentMethod;
}
//{
//    "macID": "402608202",
//    "clientOrderId": "ABKDOW",
//    "transponderType": "REGI4",
//    "licensePlate": "U55U9",
//    "timeStamp": 1733274821714,
//    "paymentMethod": "Bank"
//}
