package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ResponseDTO {
    private Boolean status;
    private String gate;

    public ResponseDTO(boolean status, String gate) {
        this.gate = gate;
        this.status = status;
    }
}