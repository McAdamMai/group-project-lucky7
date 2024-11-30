package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Permit2GateResDto {
    private String licensePlate;
    private Boolean isVerified;
    private String gateId;
}
