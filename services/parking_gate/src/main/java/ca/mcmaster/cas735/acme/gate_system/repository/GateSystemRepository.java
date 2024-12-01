package ca.mcmaster.cas735.acme.gate_system.repository;

import ca.mcmaster.cas735.acme.gate_system.model.GateSystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GateSystemRepository extends JpaRepository<GateSystemInfo,String>
{
    GateSystemInfo findByQRCode(Long QRCode);
    GateSystemInfo findByLicensePlate(String licensePlate);
    @Modifying
    @Query(value = "UPDATE t_gate_parking SET charge = :charge,fine_reason= :reason WHERE license_plate = :license_plate",
            nativeQuery = true)
    void updateGateSystemInfo(@Param("license_plate") String license_plate,
                              @Param("reason") String reason,
                              @Param("charge") Integer charge);
}
