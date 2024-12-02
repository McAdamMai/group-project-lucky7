package ca.mcmaster.cas735.acme.parking_availability.business;

import lombok.extern.slf4j.Slf4j;
import ca.mcmaster.cas735.acme.parking_availability.adapters.AMQPSender;
import ca.mcmaster.cas735.acme.parking_availability.model.LotInfo;
import ca.mcmaster.cas735.acme.parking_availability.repository.LotRepository;
import ca.mcmaster.cas735.acme.parking_availability.model.LogInfo;
import ca.mcmaster.cas735.acme.parking_availability.repository.LogRepository;
import ca.mcmaster.cas735.acme.parking_availability.model.SalesInfo;
import ca.mcmaster.cas735.acme.parking_availability.repository.SalesRepository;
import ca.mcmaster.cas735.acme.parking_availability.dto.ResponseDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.RequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.MonitorRequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.BillDTO;
import ca.mcmaster.cas735.acme.parking_availability.ports.CheckSpace;
import ca.mcmaster.cas735.acme.parking_availability.ports.Monitor;
import ca.mcmaster.cas735.acme.parking_availability.ports.AddSale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service @Slf4j
public class AvailabilityService implements CheckSpace, Monitor, AddSale {

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
    public void checkSpace(RequestDTO request) {
        boolean space;
        String lot_id = lotRepo.getLotIDByGate(request.getGate());
        if (request.getIsEnter()) {
            log.info("entrance {},", request.getGate());
            LogInfo entry = translate(request, lot_id);
            if (Objects.equals(lotRepo.compareOccupancy2Capacity(lot_id), "true")) {
                lotRepo.updateOccupancyByLotId(1, lot_id);
                logRepo.save(entry);
                space = true;
            }else {
                space = false;
            }
        } else {
            log.info("exit {},", request.getGate());
            LogInfo entry = translate(request, lot_id);
            logRepo.save(entry);
            lotRepo.updateOccupancyByLotId(-1, lot_id);
            space = true;
        }
        ResponseDTO res = new ResponseDTO(space, request.getGate());
        gateSender.sendToGate(res);
    }

    @Override
    public void monitor(MonitorRequestDTO request) {
        String lotID = request.getLot();
        List<Long> times = logRepo.findAllEntryTimes(lotID);
        Long sales = salesRepo.count();
        Integer revenue = salesRepo.totalRevenue();
        Map<Integer, Long> hourCounts = times.stream()
                .map(enterTime -> LocalDateTime.ofInstant(Instant.ofEpochMilli(enterTime), ZoneOffset.UTC))
                .map(LocalDateTime::getHour)
                .collect(Collectors.groupingBy(hour -> hour, Collectors.counting()));
        System.out.println("Overall total permit sales: " + sales);
        System.out.println("Overall total permit revenue: " + revenue);
        System.out.println("Display stats for lot " + lotID);
        System.out.println("Occupancy: " + lotRepo.getOccupancyByLotID(lotID) + " out of " + lotRepo.getCapacityByLotID(lotID));
        System.out.println("Peak usage times: ");

        hourCounts.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> System.out.println("Hour: " + entry.getKey()));
    }

    @Override
    public void addSale(BillDTO bill) {
        SalesInfo sale = new SalesInfo();
        sale.setBill(bill.getBill());
        salesRepo.save(sale);
    }

    private LogInfo translate(RequestDTO req, String lot) {
        return LogInfo.builder()
                .license(req.getLicense())
                .lot(lot)
                .permit(req.getPermit())
                .timeStamp(req.getTime())
                .isEnter(req.getIsEnter())
                .build();
    }
}
