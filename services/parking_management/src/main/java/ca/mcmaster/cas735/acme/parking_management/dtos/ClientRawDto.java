package ca.mcmaster.cas735.acme.parking_management.dtos;

import ca.mcmaster.cas735.acme.parking_management.utils.TransponderType;
import ca.mcmaster.cas735.acme.parking_management.utils.TypeOfPaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ClientRawDto {
    private String macID;
    private String clientOrderId;
    private String licensePlate;
    private TransponderType transponderType;
    private Long timeStamp;
    private TypeOfPaymentMethod paymentMethod;
}
