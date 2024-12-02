package ca.mcmaster.cas735.acme.parking_availability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Payment2AvailDTO {
    private Integer permit_sales;
    private Integer total_revenue;
    private Integer permit_revenue;
    private Integer parking_revenue;
}
//{"permit_sales":10,"total_revenue":100050,"permit_revenue":100000,"parking_revenue":50}