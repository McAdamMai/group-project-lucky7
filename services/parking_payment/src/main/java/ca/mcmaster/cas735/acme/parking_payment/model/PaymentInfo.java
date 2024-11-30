package ca.mcmaster.cas735.acme.parking_payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
    @Table(name = "t_payment_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfo {
    @Id
    private String paymentId;
    private Integer productName;
    private Integer bill;
    private String ProductId;
    private Integer paymentStatus;

}
