package ca.mcmaster.cas735.acme.gate_system.repository;

import ca.mcmaster.cas735.acme.gate_system.model.GateSystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GateSystemRepository extends JpaRepository<GateSystemInfo,String>
{
    GateSystemInfo findByQRCode(Long QRCode);
    GateSystemInfo findByLicensePlate(String licensePlate);
    void updateGateSystemInfo(GateSystemInfo gateSystemInfo);
}
