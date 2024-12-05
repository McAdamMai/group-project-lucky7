package ca.mcmaster.cas735.acme.parking_management.business;

import ca.mcmaster.cas735.acme.parking_management.dtos.AvailabilityResp;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2avlIF;
import ca.mcmaster.cas735.acme.parking_management.repository.TransponderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManagementConfig {
    private final Management2avlIF management2avlIF;
    private final TransponderRepository transponderRepository;

    @PostConstruct
    public void initSaleInfo(){
        log.info("Init sales info in repo");
        management2avlIF.send2val(new AvailabilityResp(
                transponderRepository.countTransponderExpireTime(System.currentTimeMillis())));
    }
}
