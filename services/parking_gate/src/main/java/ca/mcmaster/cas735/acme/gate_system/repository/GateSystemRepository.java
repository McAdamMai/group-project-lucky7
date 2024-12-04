package ca.mcmaster.cas735.acme.gate_system.repository;

import ca.mcmaster.cas735.acme.gate_system.model.GateSystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GateSystemRepository extends JpaRepository<GateSystemInfo,String>
{
    GateSystemInfo findByQRCode(Long QRCode);
    GateSystemInfo findByLicensePlate(String licensePlate);
    @Modifying
    @Query(value = "UPDATE t_gate SET charge = :charge,fine_reason= :reason WHERE license_plate = :license_plate",
            nativeQuery = true)
    void updateGateSystemInfo(@Param("license_plate") String license_plate,
                              @Param("reason") String reason,
                              @Param("charge") Integer charge);

    @Modifying
    @Transactional
    @Query(value = "UPDATE t_gate SET entry_time = :value WHERE qrcode = :code",
            nativeQuery = true)
    void updateEntryTimeByQrcode(@Param("value") Long time, @Param("code") Long QRCode);

    @Modifying
    @Transactional
    @Query(value = "UPDATE t_gate SET entry_time = :value WHERE license_plate = :license",
            nativeQuery = true)
    void updateEntryTimeByLicensePlate(@Param("value") Long time, @Param("license") String value);
}
