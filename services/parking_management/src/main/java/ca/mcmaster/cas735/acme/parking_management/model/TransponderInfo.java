package ca.mcmaster.cas735.acme.parking_management.model;

import ca.mcmaster.cas735.acme.parking_management.utils.TransponderType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_transponder", uniqueConstraints = {
        @UniqueConstraint(columnNames = "transponderID"),
        @UniqueConstraint(columnNames = "orderID")
})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransponderInfo{
    @Id
    private String macID;
    @Column(name = "transponderID") //can be null since transponder is given after payment done
    private String transponderID; //can be used for deduplicate key
    @Column(name = "orderID", nullable = false)
    private String orderID;
    private Integer transponderType;
    private String licensePlate;
    private Long registerTime;
    private Long expireTime;
}
// once received a requested from client, an order will be generated and store to the db.
// the initial data will be stored as: "tID", "mcID", "Plate", "rTime", "eTime = -1"
