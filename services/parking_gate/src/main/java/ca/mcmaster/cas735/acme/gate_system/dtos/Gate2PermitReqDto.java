package ca.mcmaster.cas735.acme.gate_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Gate2PermitReqDto {
    private BigInteger transponderId;
}
