package ca.mcmaster.cas735.acme.parking_payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Payment2AvailDTO {
    private Integer permit_sales;
    private Integer total_revenue;
    private Integer permit_revenue;
    private Integer parking_revenue;

    public Payment2AvailDTO(Integer permit_sales, Integer total_revenue, Integer permit_revenue, Integer parking_revenue) {
        this.permit_sales = permit_sales;
        this.total_revenue = total_revenue;
        this.permit_revenue = permit_revenue;
        this.parking_revenue = parking_revenue;
    }

}