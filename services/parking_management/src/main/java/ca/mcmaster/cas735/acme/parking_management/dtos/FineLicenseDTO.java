package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FineLicenseDTO {
    private Long timeStamp;
    private String license;
    private String reason;
}