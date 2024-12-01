package ca.mcmaster.cas735.acme.parking_enforcement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FineLicenseDTO {
    String timeStamp;
    String license;
    String reason;
}
