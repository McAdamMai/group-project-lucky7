package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ResponseDTO {
    private Boolean status;

    public ResponseDTO(boolean status) {
        this.status = status;
    }
}