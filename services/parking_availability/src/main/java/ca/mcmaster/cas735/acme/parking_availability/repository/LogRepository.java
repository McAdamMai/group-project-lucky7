package ca.mcmaster.cas735.acme.parking_availability.repository;

import ca.mcmaster.cas735.acme.parking_availability.model.LogInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogInfo,Long> {
    LogInfo findByLicense(String license);
    //@Query("SELECT l.enterTime FROM t_loginfo l WHERE l.lot = :lot AND l.isEnter = true")
    //List<Long> findAllEntryTimes(@Param("lot") String lot);

    @Modifying
    @Transactional
    @Query(value = "UPDATE t_loginfo l SET l.exit_time = :exit_time WHERE l.license = :license AND l.is_enter = true AND l.exit_time = -1",
            nativeQuery = true)
    void updateExitTime(@Param("license") String license, @Param("exit_time") long exit_time);
}