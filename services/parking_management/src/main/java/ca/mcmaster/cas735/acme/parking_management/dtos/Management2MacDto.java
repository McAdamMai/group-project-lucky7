package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Management2MacDto {
    private String licensePlate;
    private String macID;
    private String transponderID;
    private Long timeStamp;
}
