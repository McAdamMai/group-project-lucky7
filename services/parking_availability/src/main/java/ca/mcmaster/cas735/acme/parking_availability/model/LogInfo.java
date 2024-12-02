package ca.mcmaster.cas735.acme.parking_availability.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

@Entity
@Table(name = "t_loginfo")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogInfo{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String license;
    private String lot;
    private String permit;
    private Long timeStamp;
    private boolean isEnter;
}
