package ca.mcmaster.cas735.acme.gate_system.dtos;

import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class GateCheckSpaceDto implements Serializable {
    private String gate;
    private Boolean isEnter;

    public GateCheckSpaceDto(String gate, Boolean isEnter) {
        this.gate = gate;
        this.isEnter = isEnter;
    }
}

