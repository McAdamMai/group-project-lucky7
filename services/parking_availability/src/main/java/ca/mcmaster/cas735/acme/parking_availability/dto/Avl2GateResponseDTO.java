package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Avl2GateResponseDTO {
    private Boolean status;
    private String gate;

    public Avl2GateResponseDTO(boolean status, String gate) {
        this.gate = gate;
        this.status = status;
    }
}