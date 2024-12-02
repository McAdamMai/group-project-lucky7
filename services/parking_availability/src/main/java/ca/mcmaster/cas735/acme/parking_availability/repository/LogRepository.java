package ca.mcmaster.cas735.acme.parking_availability.repository;

import ca.mcmaster.cas735.acme.parking_availability.model.LogInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogInfo,Long>
{
    LogInfo findByLicense(String license);
    
    @Query("SELECT l.timeStamp FROM LogInfo l WHERE l.lot = :lot AND l.isEnter = true")
    List<Long> findAllEntryTimes(@Param("lot") String lot);
}