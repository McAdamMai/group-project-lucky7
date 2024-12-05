package ca.mcmaster.cas735.acme.parking_availability.business;

import ca.mcmaster.cas735.acme.parking_availability.dto.*;
import ca.mcmaster.cas735.acme.parking_availability.model.SalesInfo;
import ca.mcmaster.cas735.acme.parking_availability.ports.GateReq;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ca.mcmaster.cas735.acme.parking_availability.repository.LotRepository;
import ca.mcmaster.cas735.acme.parking_availability.model.LogInfo;
import ca.mcmaster.cas735.acme.parking_availability.repository.LogRepository;
import ca.mcmaster.cas735.acme.parking_availability.repository.SalesRepository;
import ca.mcmaster.cas735.acme.parking_availability.ports.UpdateSpace;
import ca.mcmaster.cas735.acme.parking_availability.ports.Monitor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;


@Service @Slf4j
public class AvailabilityService implements UpdateSpace, Monitor, GateReq {

    private final SalesRepository salesRepo;
    private final LogRepository logRepo;
    private final LotRepository lotRepo;

    @Autowired
    public AvailabilityService(SalesRepository salesRepo,
                               LogRepository logRepo,
                               LotRepository lotRepo) {
        this.salesRepo = salesRepo;
        this.logRepo = logRepo;
        this.lotRepo = lotRepo;
    }

    @Override
    @Transactional
    public void updateSpace(Gate2AvailabilityResDto request) {
        String lot_id = lotRepo.getLotIDByGate(request.getGate());
        if (request.getIsEnter()) {
            log.info("entrance {},", request.getGate());
            LogInfo parkingRecord = translate2Enter(request, lot_id);
            if (Objects.equals(lotRepo.compareOccupancy2Capacity(lot_id), "true") &&
                    logRepo.existsByLicense(request.getLicensePlate()) != 1L) {
                log.info("add a new log record {}", parkingRecord);
                logRepo.save(parkingRecord);
                lotRepo.updateOccupancyByLotId(1, lot_id);
            }
        } else { //exit
            log.info("exit {},", request.getGate());
            if(logRepo.existsByLicense(request.getLicensePlate()) == 1L){
                logRepo.updateExitTime(request.getLicensePlate(), request.getTime());
                lotRepo.updateOccupancyByLotId(-1, lot_id);
            }
        }
    }

    @Override
    public String monitor() {
        List<Object[]> hour_lists = logRepo.countHour();
        Map<Integer, Integer> hourlyCount = new HashMap<>();
        // get hourly count
        for (Object[] hour_list : hour_lists) {
            Integer hour = ((Number) hour_list[0]).intValue();
            Integer count = ((Number) hour_list[1]).intValue();
            hourlyCount.put(hour, count);
        }
        Avl2Monitor avl2Monitor = new Avl2Monitor();
        SalesInfo salesInfo = salesRepo.findAllById(1);
        avl2Monitor.setParking_revenue(salesInfo.getParking_revenue());
        avl2Monitor.setPermit_sales(salesInfo.getPermit_sales());
        avl2Monitor.setPermit_revenue(salesInfo.getPermit_revenue());
        avl2Monitor.setTotal_revenue(salesInfo.getTotal_revenue());
        avl2Monitor.setValid_permits(salesInfo.getValid_permits());
        avl2Monitor.setHourly_count(hourlyCount);
        log.info("statistic {}", avl2Monitor);
        return dto2json(avl2Monitor);
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
                .hour(timeStamp2Hour(req.getTime()))
                .typeOfClient(req.getTypeOfClient().name())
                .isEnter(req.getIsEnter())
                .build();
    }

    private int timeStamp2Hour(Long timeStamp) {
        Date date = new Date(timeStamp);
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        return Integer.parseInt(hourFormat.format(date));
    }

    private <T> String dto2json(T dto){
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload;
        try{
            jsonPayload = objectMapper.writeValueAsString(dto);
            return jsonPayload;
        }catch (JsonProcessingException e){
            throw new RuntimeException();
        }
    }
}
