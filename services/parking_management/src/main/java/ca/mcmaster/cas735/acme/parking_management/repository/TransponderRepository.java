package ca.mcmaster.cas735.acme.parking_management.repository;

import ca.mcmaster.cas735.acme.parking_management.model.TransponderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransponderRepository extends JpaRepository<TransponderInfo,String>
{
    TransponderInfo findByMacID(String macID); //additional method to find by macID
    boolean existsByMacID(String macID);
    boolean existsByOrderID(String orderID);
    void deleteByOrderID(String orderID);

    @Query(value = "UPDATE parking-transponder SET registerTime = :registerTime WHERE macID = :macID",
    nativeQuery = true)
    void updateTransponderRegisterTime(@Param("macID") String macID,
                              @Param("registerTime") Long registerTime);//return the changed line

    @Query(value = "UPDATE parking-transponder SET expireTime = :expireTime WHERE macID = :macID",
            nativeQuery = true)
    void updateTransponderExpiryTime(@Param("macID") String macID,
                                       @Param("expireTime") Long expireTime);//return the changed line
}
