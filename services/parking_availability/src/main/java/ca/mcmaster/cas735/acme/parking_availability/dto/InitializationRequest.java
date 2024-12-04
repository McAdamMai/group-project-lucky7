package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class InitializationRequest {
    private Boolean key;

    public InitializationRequest(Boolean key) {
        this.key = key;
    }
}
