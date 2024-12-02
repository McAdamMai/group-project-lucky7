package ca.mcmaster.cas735.acme.parking_availability.repository;

import ca.mcmaster.cas735.acme.parking_availability.model.SalesInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesRepository extends JpaRepository<SalesInfo,Long>
{

    @Query("SELECT SUM(s.bill) FROM SalesInfo s")
    Integer totalRevenue();
}