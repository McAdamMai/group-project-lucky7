package ca.mcmaster.cas735.acme.parking_availability.model;

import jakarta.persistence.*;
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
    private Integer id;

    private Integer valid_permits;
    private Integer permit_sales;
    private Integer total_revenue;
    private Integer permit_revenue;
    private Integer parking_revenue;
}
