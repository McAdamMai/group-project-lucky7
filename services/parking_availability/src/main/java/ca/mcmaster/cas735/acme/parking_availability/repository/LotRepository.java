package ca.mcmaster.cas735.acme.parking_availability.repository;

import ca.mcmaster.cas735.acme.parking_availability.model.LotInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<LotInfo,Long>
{
    LotInfo findByEnterGate(String gate);

    LotInfo findByExitGate(String gate);

    LotInfo findByLotID(String lot);

    @Modifying
    @Query(value = "UPDATE t_lotinfo SET occupancy = occupancy + :increment WHERE lotid = :lotid",
            nativeQuery = true)
    void updateOccupancyByLotId(Integer increment, String lotid);

    @Query(value = "SELECT CASE WHEN l.occupancy < l.capacity THEN 'true' ELSE 'false' END " +
            "AS result FROM t_lotinfo l WHERE l.lotid = :lotid ", nativeQuery = true)
    String compareOccupancy2Capacity(@Param("lotid")String lotid);

    @Query(value ="SELECT l.occupancy FROM t_lotinfo l WHERE l.lotID = :lotid", nativeQuery = true)
    Integer getOccupancyByLotID(@Param("lotid") String lotid);

    @Query(value ="SELECT l.capacity FROM t_lotinfo l WHERE l.lotID = :lotid", nativeQuery = true)
    Integer getCapacityByLotID(@Param("lotid") String lotid);

    @Query(value = "SELECT l.lotID FROM t_lotinfo l WHERE l.enter_gate = :gate OR l.exit_gate = :gate",
            nativeQuery = true)
    String getLotIDByGate(@Param("gate") String gate);
}

