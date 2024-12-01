package ca.mcmaster.cas735.acme.gate_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingInfoRequest {
    private String licensePlate;
    private Integer charge;
    private Boolean isVisitor;
    private Long entryTime;
}
