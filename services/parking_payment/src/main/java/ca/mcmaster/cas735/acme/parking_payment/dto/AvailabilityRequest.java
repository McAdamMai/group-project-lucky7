package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AvailabilityRequest {
    private Boolean key;

    public AvailabilityRequest(Boolean key) {
        this.key = key;
    }
}
