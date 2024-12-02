package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AvailabilityResp {
    private Integer valid_permits;

    public AvailabilityResp(Integer valid_permits) {
        this.valid_permits = valid_permits;
    }
}
