package ca.mcmaster.cas735.acme.parking_payment.repository;

import ca.mcmaster.cas735.acme.parking_payment.model.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
    PaymentInfo findByPaymentId(String PaymentID);

    @Modifying
    @Query(value = "UPDATE t_payment_info SET payment_status = :status WHERE payment_id =:paymentID", nativeQuery = true)
    void updatePaymentStatus(@Param( "paymentID") String paymentID,
                             @Param("status") Integer status);
}

