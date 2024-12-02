package ca.mcmaster.cas735.acme.parking_availability.config;

import ca.mcmaster.cas735.acme.parking_availability.repository.LotRepository;
import ca.mcmaster.cas735.acme.parking_availability.model.LotInfo;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class DataInit {

    private final LotRepository lotRepository;

    public DataInit(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    // use a yaml to store this data
    @PostConstruct
    public void initData() {
        if (lotRepository.count() == 0) {
            lotRepository.saveAll(List.of(
                LotInfo.builder()
                    .lotID("12345")
                    .capacity(100)
                    .occupancy(0)
                    .enterGate("ENTRY12345")
                    .exitGate("EXIT12345")
                    .build(),
                LotInfo.builder()
                    .lotID("54321")
                    .capacity(200)
                    .occupancy(0)
                    .enterGate("ENTRY54321")
                    .exitGate("EXIT54321")
                    .build(),
                LotInfo.builder()
                    .lotID("67890")
                    .capacity(200)
                    .occupancy(0)
                    .enterGate("ENTRY67890")
                    .exitGate("EXIT67890")
                    .build()
            ));
            System.out.println("Init parking lots in repo");
        }
    }
}