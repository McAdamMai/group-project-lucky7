package ca.mcmaster.cas735.acme.parking_payment.model;

import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfOrder;
import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfPaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "t_paymentInfo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfo {
    @Id
    private String paymentId;
    private TypeOfOrder productName;
    private Integer bill;
    private String productID;
    private TypeOfPaymentStatus paymentStatus;

}
