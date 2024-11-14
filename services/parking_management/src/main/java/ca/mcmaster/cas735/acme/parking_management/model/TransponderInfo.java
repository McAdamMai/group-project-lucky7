package ca.mcmaster.cas735.acme.parking_management.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "t_transponder")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransponderInfo{
    @Id
    private String macID;
    private String licensePlate;
    //private BigInteger transponderID;
    private Integer registerTime;
    private Integer expireTime;
}
