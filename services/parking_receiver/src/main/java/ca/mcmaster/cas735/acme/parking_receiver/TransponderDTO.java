package ca.mcmaster.cas735.acme.parking_receiver;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TransponderDTO {
    String t;
    String n;
    String v;
}
