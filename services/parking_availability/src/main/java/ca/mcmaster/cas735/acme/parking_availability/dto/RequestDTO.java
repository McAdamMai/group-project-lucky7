package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RequestDTO {
    private Boolean isEnter;
    private String permit;
    private String time;
    private String license;
}