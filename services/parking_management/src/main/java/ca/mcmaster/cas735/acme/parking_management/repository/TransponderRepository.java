package ca.mcmaster.cas735.acme.parking_management.repository;

import ca.mcmaster.cas735.acme.parking_management.model.TransponderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransponderRepository extends JpaRepository<TransponderInfo,String>
{
    TransponderInfo findByMacID(String macID); //additional method to find by macID
    boolean existsByMacID(String macID);
    boolean existsByOrderID(String orderID);
    boolean existsByTransponderID(String transponderID);
    void deleteByOrderID(String orderID);

    @Query(value = "Select expire_time FROM t_transponder WHERE macid = :value",
            nativeQuery = true)
    Long getExpireTimeByMacId( @Param("value") String value);

    @Query(value = "Select expire_time FROM t_transponder WHERE orderid = :value",
            nativeQuery = true)
    Long getExpireTimeByOrderId( @Param("value") String value);

    @Query(value = "Select expire_time FROM t_transponder WHERE transponderid = :value",
            nativeQuery = true)
    Long getExpireTimeByOrderTId( @Param("value") String value);

    @Query(value = "Select license_plate FROM t_transponder WHERE transponderid = :value",
        nativeQuery = true)
    String getLicensePlateByOrderTId( @Param("value") String value);

    @Modifying //add modifying to indicate we are modifying data but not retrieving
    @Query(value = "UPDATE t_transponder SET register_time = expire_time WHERE macid = :macid",
    nativeQuery = true)
    void updateTransponderRegisterTime(@Param("macid") String macid);//return the changed line

    @Modifying
    @Query(value = "UPDATE t_transponder SET register_time = :register_time WHERE macid = :macid",
            nativeQuery = true)
    void updateTransponderRegisterTimeEx(@Param("macid") String macid,
                                         @Param("register_time") Long register_time);//return the changed line

    @Modifying
    @Query(value = "UPDATE t_transponder SET expire_time = :expire_time WHERE macid = :macid",
            nativeQuery = true)
    void updateTransponderExpiryTime(@Param("macid") String macid,
                                       @Param("expire_time") Long expire_time);//return the changed line

    @Modifying
    @Query(value = "UPDATE t_transponder SET orderid = :orderid WHERE macid = :macid",
    nativeQuery = true)
    void updateTransponderOrderId(@Param("macid") String macid,
                                  @Param("orderid") String orderid);
}
