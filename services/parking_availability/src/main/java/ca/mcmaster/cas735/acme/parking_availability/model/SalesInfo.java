package ca.mcmaster.cas735.acme.parking_availability.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

@Entity
@Table(name = "t_saleinfo")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesInfo{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer valid_permits;
    private Integer permit_sales;
}
