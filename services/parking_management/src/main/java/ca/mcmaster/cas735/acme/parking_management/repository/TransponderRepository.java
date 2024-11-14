package ca.mcmaster.cas735.acme.parking_management.repository;

import ca.mcmaster.cas735.acme.parking_management.model.TransponderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransponderRepository extends JpaRepository<TransponderInfo,String>
{
    TransponderInfo findByMacID(String macID); //additional method to find by macID
}
