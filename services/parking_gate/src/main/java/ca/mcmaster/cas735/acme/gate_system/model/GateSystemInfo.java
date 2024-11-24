package ca.mcmaster.cas735.acme.gate_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.math.BigDecimal;


// Need to add @Document(value = "product"), but for a JPA
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GateSystemInfo {
    @Id
    private String licensePlate;
    private Long QRCode;
    private BigDecimal charge;
    private String fineReason;
    private Boolean isVisitor;
    private Long entryTime;
}
