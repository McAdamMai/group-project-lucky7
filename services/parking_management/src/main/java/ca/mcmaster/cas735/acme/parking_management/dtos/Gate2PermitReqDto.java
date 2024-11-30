package ca.mcmaster.cas735.acme.parking_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Gate2PermitReqDto {
    private String transponderId;
    private String gateId;
}
//{"transponderId": "338b3c6b10aa551a", "gateId": "Gate10"}
//{"transponderId": "338b3c6b10aa551b", "gateId": "Gate10"}