package ca.mcmaster.cas735.acme.parking_availability.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "t_lotinfo")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotInfo{
    @Id
    private String lotID;
    private Integer capacity;
    private Integer occupancy;
    private String enterGate;
    private String exitGate;
}
