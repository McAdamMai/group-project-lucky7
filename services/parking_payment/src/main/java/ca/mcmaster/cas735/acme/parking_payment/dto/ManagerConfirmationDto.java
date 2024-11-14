package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManagerConfirmationDto {
    private String licensePlate;
    private String macID;
    private Integer timeStamp;
    private boolean paymentStatus;
}
