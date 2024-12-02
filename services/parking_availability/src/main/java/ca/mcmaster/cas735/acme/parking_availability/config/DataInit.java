package ca.mcmaster.cas735.acme.parking_availability.config;

import ca.mcmaster.cas735.acme.parking_availability.dto.AvailabilityRequest;
import ca.mcmaster.cas735.acme.parking_availability.model.SalesInfo;
import ca.mcmaster.cas735.acme.parking_availability.ports.AddSale;
import ca.mcmaster.cas735.acme.parking_availability.repository.LotRepository;
import ca.mcmaster.cas735.acme.parking_availability.model.LotInfo;
import ca.mcmaster.cas735.acme.parking_availability.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInit{

    private final LotRepository lotRepository;
    private final SalesRepository salesRepository;
    private final AddSale addSale;

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
        salesRepository.save(SalesInfo.builder()
                .id(1)
                .valid_permits(0)
                .permit_sales(0)
                .parking_revenue(0)
                .permit_revenue(0)
                .total_revenue(0)
                .build()
        );
    }
    @PostConstruct
    public void initSaleInfo(){
        log.info("Init sales info in repo");
        addSale.request2Payment(new AvailabilityRequest(true));
        addSale.request2Management(new AvailabilityRequest(true));
    }
}