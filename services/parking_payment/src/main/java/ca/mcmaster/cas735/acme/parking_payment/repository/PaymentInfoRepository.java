package ca.mcmaster.cas735.acme.parking_payment.repository;

import ca.mcmaster.cas735.acme.parking_payment.model.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
    PaymentInfo findByPaymentId(String PaymentID);

    @Modifying
    @Query(value = "UPDATE t_payment SET payment_status = :status WHERE payment_id =:paymentID", nativeQuery = true)
    void updatePaymentStatus(@Param( "paymentID") String paymentID,
                             @Param("status") Integer status);

    @Query(value = "SELECT COUNT(l.bill) FROM t_payment l WHERE l.product_name = 2 and l.payment_status = 2",
            nativeQuery = true)
    Integer countTransponderSales();

    @Query(value = "SELECT SUM(l.bill) FROM t_payment l WHERE l.product_name = 2 and l.payment_status = 2",
            nativeQuery = true)
    Integer SumTransponderSales();

    @Query(value = "SELECT SUM(l.bill) FROM t_payment l WHERE l.product_name = 1 and l.payment_status = 2",
            nativeQuery = true)
    Integer SumParkingSales();

    @Query(value = "SELECT SUM(l.bill) FROM t_payment l WHERE l.payment_status = 2",
            nativeQuery = true)
    Integer SumSales();
}

