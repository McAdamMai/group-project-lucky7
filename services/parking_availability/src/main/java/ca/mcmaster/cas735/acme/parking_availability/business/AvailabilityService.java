package ca.mcmaster.cas735.acme.parking_availability.business;

import ca.mcmaster.cas735.acme.parking_availability.dto.GateCheckSpaceDto;
import ca.mcmaster.cas735.acme.parking_availability.ports.GateReq;
import ca.mcmaster.cas735.acme.parking_availability.utils.TypeOfClient;
import ca.mcmaster.cas735.acme.parking_availability.utils.TypeOfClientInt;
import lombok.extern.slf4j.Slf4j;
import ca.mcmaster.cas735.acme.parking_availability.adapters.AMQPSender;
import ca.mcmaster.cas735.acme.parking_availability.repository.LotRepository;
import ca.mcmaster.cas735.acme.parking_availability.model.LogInfo;
import ca.mcmaster.cas735.acme.parking_availability.repository.LogRepository;
import ca.mcmaster.cas735.acme.parking_availability.repository.SalesRepository;
import ca.mcmaster.cas735.acme.parking_availability.dto.Avl2GateResponseDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.Gate2AvailabilityResDto;
import ca.mcmaster.cas735.acme.parking_availability.dto.MonitorRequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.ports.UpdateSpace;
import ca.mcmaster.cas735.acme.parking_availability.ports.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service @Slf4j
public class AvailabilityService implements UpdateSpace, Monitor, GateReq {

    private final SalesRepository salesRepo;
    private final LogRepository logRepo;
    private final LotRepository lotRepo;
    private final AMQPSender gateSender;

    @Autowired
    public AvailabilityService(SalesRepository salesRepo,
                               LogRepository logRepo,
                               LotRepository lotRepo,
                               AMQPSender gateSender) {
        this.salesRepo = salesRepo;
        this.logRepo = logRepo;
        this.lotRepo = lotRepo;
        this.gateSender = gateSender;
    }

    @Override
    @Transactional
    public void updateSpace(Gate2AvailabilityResDto request) {
        String lot_id = lotRepo.getLotIDByGate(request.getGate());
        if (request.getIsEnter()) {
            log.info("entrance {},", request.getGate());
            LogInfo parkingRecord = translate2Enter(request, lot_id);
            if (Objects.equals(lotRepo.compareOccupancy2Capacity(lot_id), "true")) {
                logRepo.save(parkingRecord);
                lotRepo.updateOccupancyByLotId(1, lot_id);
            }
        } else { //exit
            log.info("exit {},", request.getGate());
            logRepo.updateExitTime(request.getLicensePlate(), request.getTime());
            lotRepo.updateOccupancyByLotId(-1, lot_id);
        }
    }

    @Override
    public void monitor(MonitorRequestDTO request) {

        /*String lotID = request.getLot();
        List<Long> times = logRepo.findAllEntryTimes(lotID);
        Long sales = salesRepo.count();
        //Integer revenue = salesRepo.totalRevenue();
        Map<Integer, Long> hourCounts = times.stream()
                .map(enterTime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(enterTime), ZoneOffset.UTC))
                .map(LocalDateTime::getHour)
                .collect(Collectors.groupingBy(hour -> hour, Collectors.counting()));
        System.out.println("Overall total permit sales: " + sales);
        //System.out.println("Overall total permit revenue: " + revenue);
        System.out.println("Display stats for lot " + lotID);
        System.out.println("Occupancy: " + lotRepo.getOccupancyByLotID(lotID) + " out of " + lotRepo.getCapacityByLotID(lotID));
        System.out.println("Peak usage times: ");
        hourCounts.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> System.out.println("Hour: " + entry.getKey()));*/
    }

    @Override
    public boolean checkSpace(GateCheckSpaceDto gateCheckSpaceDto) {
        String lot_id = lotRepo.getLotIDByGate(gateCheckSpaceDto.getGate());
        log.info("receive checkspace {}", gateCheckSpaceDto.getGate());
        if(Objects.equals(lotRepo.compareOccupancy2Capacity(lot_id), "true")){
            return true;
        }else{ //no space
            return false;
        }
    }

    private LogInfo translate2Enter(Gate2AvailabilityResDto req, String lot) {
        return LogInfo.builder()
                .license(req.getLicensePlate())
                .lot(lot)
                .enterTime(req.getTime())
                .exitTime(-1L) //waiting for exit
                .typeOfClient(req.getTypeOfClient().name())
                .isEnter(req.getIsEnter())
                .build();
    }
}
