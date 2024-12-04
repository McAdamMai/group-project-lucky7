package ca.mcmaster.cas735.acme.parking_enforcement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MemberDTO {
    private String license;
    private String macID;
    private Long timeStamp;
    private String reason;
    private Boolean found;
}