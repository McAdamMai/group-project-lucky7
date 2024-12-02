package ca.mcmaster.cas735.acme.parking_availability.repository;

import ca.mcmaster.cas735.acme.parking_availability.model.SalesInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SalesRepository extends JpaRepository<SalesInfo, Integer>
{
    Integer findAllById(int id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE t_saleinfo l SET l.permit_sales = :c0  WHERE l.id = 1", nativeQuery = true)
    void updatePermitSale(@Param("c0")int c0);

    @Transactional
    @Modifying
    @Query(value = "UPDATE t_saleinfo l SET l.total_revenue = :c1  WHERE l.id = 1", nativeQuery = true)
    void updateTotalRevenue(@Param("c1")int c1);

    @Transactional
    @Modifying
    @Query(value = "UPDATE t_saleinfo l SET l.permit_revenue = :c2 WHERE l.id = 1", nativeQuery = true)
    void updatePermitRevenue(@Param("c2")int c2);

    @Transactional
    @Modifying
    @Query(value = "UPDATE t_saleinfo l SET l.parking_revenue = :c3 WHERE l.id = 1", nativeQuery = true)
    void updateParkingRevenue(@Param("c3")int c3);

    @Transactional
    @Modifying
    @Query(value = "UPDATE t_saleinfo l SET l.valid_permits = :c4 WHERE l.id = 1", nativeQuery = true)
    void updateValidPermits(@Param("c4")int c4);


}