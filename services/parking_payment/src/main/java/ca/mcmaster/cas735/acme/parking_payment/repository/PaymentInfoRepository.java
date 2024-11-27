package ca.mcmaster.cas735.acme.parking_payment.repository;

import ca.mcmaster.cas735.acme.parking_payment.model.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
    PaymentInfo findByPaymentId(String PaymentID);
}

