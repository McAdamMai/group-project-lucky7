package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class Avl2Monitor {
    private Integer parking_revenue;
    private Integer permit_revenue;
    private Integer permit_sales;
    private Integer total_revenue;
    private Integer valid_permits;
    private Map<Integer, Integer> hourly_count;
}
