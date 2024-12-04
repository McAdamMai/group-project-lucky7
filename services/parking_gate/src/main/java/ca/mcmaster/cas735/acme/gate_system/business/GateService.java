package ca.mcmaster.cas735.acme.gate_system.business;

import ca.mcmaster.cas735.acme.gate_system.adaptors.SenderGateSystem;
import ca.mcmaster.cas735.acme.gate_system.dtos.*;
import ca.mcmaster.cas735.acme.gate_system.model.GateSystemInfo;
import ca.mcmaster.cas735.acme.gate_system.ports.GateIF;
import ca.mcmaster.cas735.acme.gate_system.repository.GateSystemRepository;
import ca.mcmaster.cas735.acme.gate_system.utils.TypeOfClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GateService {
    private final boolean isFull = false;
    private final GateSystemRepository gateSystemRepository;
    private final GateIF gateIF;
    private final SenderGateSystem senderGateSystem;
    private final WebClient webClient;

    private static final Set<String> exitGates = new HashSet<>(Arrays.asList("EXIT12345", "EXIT54321", "EXIT67890"));
    private static final Set<String> entryGates = new HashSet<>(Arrays.asList("ENTRY12345", "ENTRY54321", "ENTRY67890"));

    //Save transponder to Repository
    public void saveTransponder(ParkingInfoRequest parkingInfoRequest) {
        GateSystemInfo gateSystemInfo = GateSystemInfo.builder()
                .licensePlate(parkingInfoRequest.getLicensePlate())
                .charge(parkingInfoRequest.getCharge())
                .QRCode(generateQRCode())
                .entryTime(parkingInfoRequest.getEntryTime())
                .isVisitor(parkingInfoRequest.getIsVisitor())
                .build();
        gateSystemRepository.save(gateSystemInfo);
        log.info("Car {} is saved", gateSystemInfo.getLicensePlate());
    }

    //Remove transponder from repository
    public void removeTransponder(String licensePlate) {
        GateSystemInfo gateSystemInfo = gateSystemRepository.findByLicensePlate(licensePlate);
        if (gateSystemInfo == null) {
            log.error("LicensePlate {} not found", licensePlate);
            return;
        }
        gateSystemRepository.delete(gateSystemInfo);
        log.info("Car {} is removed", gateSystemInfo.getLicensePlate());
    }

    // Transponder exit and enter
    public void enterExitParkingLotWithTransponder(String licensePlate, String gate) throws JsonProcessingException {
        // This is after being validated
        if (exitGates.contains(gate)) {
            if(gateSystemRepository.findByLicensePlate(licensePlate) == null){
                log.info("cannot find the corresponding parking information");
                return;
            }
            removeTransponder(licensePlate);
            // update availability of parking spots
            senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                    .isEnter(false)
                    .licensePlate(licensePlate)
                    .time(System.currentTimeMillis())
                    .gate(gate)
                    .typeOfClient(TypeOfClient.USERCLIENT).build());
        } else if (entryGates.contains(gate)) {
            Boolean space = getSpaceAvailability(gate);
            log.info("parking lot availability {}", space);
            if (!space) {
                log.info("This parking lot is full");
                return;
            }
            Long current_time = System.currentTimeMillis();
            if(gateSystemRepository.findByLicensePlate(licensePlate) == null){ // create a new record if it's empty
                saveTransponder(ParkingInfoRequest.builder()
                        .licensePlate(licensePlate)
                        .charge(0)
                        .entryTime(current_time)
                        .isVisitor(false)
                        .build());
                // update availability of parking spots
                senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                        .isEnter(true)
                        .licensePlate(licensePlate)
                        .time(current_time)
                        .gate(gate)
                        .typeOfClient(TypeOfClient.USERCLIENT).build());
            }
            else{
                log.info("license plate exists, update enter time");
                gateSystemRepository.updateEntryTimeByLicensePlate(current_time, licensePlate); // if record exists, only modify time
            }
        }else {
            log.error("Invalid gate: {}", gate);
        }
        gateIF.openGate(gate);
    }

    //
    public void enterParkingLotWithoutTransponder(String licensePlate, String gate) throws JsonProcessingException {
        // generate QR code
        if(exitGates.contains(gate)){
            log.info("Unexpected behavior, please scan the qr code");
            return;
        }
        Boolean space = getSpaceAvailability(gate);
        log.info("parking lot availability {}", space);
        if (!space) {
            log.info("This parking lot is full");
            return;
        }
        Long entryTime = System.currentTimeMillis();
        if(gateSystemRepository.findByLicensePlate(licensePlate) == null){
            long qrCode = generateQRCode();
            log.info("Generated a new QR code: {}", qrCode);
            //modified: two system.currentTimeMills should be aligned
            GateSystemInfo gateSystemInfo = GateSystemInfo.builder()
                    .licensePlate(licensePlate)
                    .charge(0)
                    .QRCode(qrCode)
                    .entryTime(entryTime)
                    .isVisitor(false)
                    .build();
            gateSystemRepository.save(gateSystemInfo);
            // update availability of parking spots
            senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                    .isEnter(true)
                    .licensePlate(licensePlate)
                    .time(entryTime)
                    .gate(gate)
                    .typeOfClient(TypeOfClient.PAYPERHOUR).build());
            gateIF.generateQRCode(qrCode);// send the QR code to mqtt service
        }
        else{
            log.info("license plate exists, update enter time");
            gateSystemRepository.updateEntryTimeByLicensePlate(entryTime, licensePlate);
        }
        gateIF.openGate(gate);
    }

    // Assigned by officer
    public void enterExitParkingLotWithQR(Long QRCode, String gate) throws JsonProcessingException { //for guest?
        if(entryGates.contains(gate)) {
            Boolean space = getSpaceAvailability(gate);
            log.info("parking lot availability {}", space);
            if (!space) {
                log.info("This parking lot is full");
                return;
            }
            GateSystemInfo gateSystemInfo = gateSystemRepository.findByQRCode(QRCode);
            log.info("find gate info {}", gateSystemInfo);
            if (gateSystemInfo != null && gateSystemInfo.getIsVisitor()) {
                Long current_time = System.currentTimeMillis();
                log.info("update time {}", current_time);
                gateSystemRepository.updateEntryTimeByQrcode(current_time, QRCode);
                senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                        .isEnter(true)
                        .licensePlate(gateSystemInfo.getLicensePlate())
                        .time(current_time)
                        .gate(gate)
                        .typeOfClient(TypeOfClient.VISITOR).build());
                gateIF.openGate(gate);
                return;
            } else{
                log.error("Invalid QR: {} or invalid visitor", QRCode);
            }
        }
        if (exitGates.contains(gate)) {
            GateSystemInfo gateSystemInfo = gateSystemRepository.findByQRCode(QRCode);
            log.info("find gate info {}", gateSystemInfo);
            if (gateSystemInfo != null && gateSystemInfo.getIsVisitor()
                    && gateSystemInfo.getCharge() == 0 ) { //TODO: add fine decision making
                removeTransponder(gateSystemInfo.getLicensePlate());
                senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                        .isEnter(false)
                        .licensePlate(gateSystemInfo.getLicensePlate())
                        .time(System.currentTimeMillis())
                        .gate(gate)
                        .typeOfClient(TypeOfClient.VISITOR).build());
                gateIF.openGate(gate);
            }
            else if(gateSystemInfo != null){
                log.info("Pending payment");
                createSendPaymentRequest(QRCode, gate); // non visitor?
            }
            else{
                log.info("cannot find the corresponding parking information");
            }
        }
    }

    public void createSendPaymentRequest(Long QRCode, String gate) {
        Gate2PaymentReqDto gate2PaymentReqDto = computeParkingPrice(QRCode, gate);
        log.info("Computed parking price: {}", gate2PaymentReqDto.getBill());
        if(gate2PaymentReqDto != null){
            senderGateSystem.sendPaymentRequest(gate2PaymentReqDto);
        }
    }

    public Gate2PaymentReqDto computeParkingPrice(Long QRCode, String gate) {
        GateSystemInfo gateSystemInfo = gateSystemRepository.findByQRCode(QRCode);
        if (gateSystemInfo == null) {
            log.error("QRCode {} not found", QRCode);
            return null;
        }
        if (gateSystemInfo.getIsVisitor()) {
            double totalCharge = gateSystemInfo.getCharge().doubleValue();
            log.info("A fine charge for car {} is {}", gateSystemInfo.getLicensePlate(), totalCharge);
            return Gate2PaymentReqDto.builder()
                    .licensePlate(gateSystemInfo.getLicensePlate())
                    .bill((int) totalCharge)
                    .gateID(gate)
                    .timeStamp((Long) System.currentTimeMillis())
                    .build();
        }
        else{
            double hourlyRate = getHourlyRate(gateSystemInfo.getEntryTime());
            long hoursStayed = calculateHoursStayed(gateSystemInfo.getEntryTime());
            log.info("hourly rate {} + hours stayed {}", hourlyRate, hoursStayed);
            double totalCharge = hourlyRate * hoursStayed + gateSystemInfo.getCharge().doubleValue();
            // Send the total charge to the payment system
            log.info("Total charge for car {} is {}", gateSystemInfo.getLicensePlate(), totalCharge);
            return Gate2PaymentReqDto.builder()
                    .licensePlate(gateSystemInfo.getLicensePlate())
                    .bill((int) totalCharge)
                    .gateID(gate)
                    .timeStamp((Long) System.currentTimeMillis())
                    .build();
        }
        // Assuming you have a method to get the hourly rate based on the day
    }

    public void exitingCar(Payment2GateResDto payment2GateResDto) {
        // update availability of parking spots
        if(payment2GateResDto.getPaymentStatus() != 2){
            log.info("Payment Failed, Please Retry");
            return;
        }
        removeTransponder(payment2GateResDto.getLicensePlate());
        senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                .isEnter(false)
                .licensePlate(payment2GateResDto.getLicensePlate())
                .time(System.currentTimeMillis())
                .gate(payment2GateResDto.getGateId())
                .typeOfClient(TypeOfClient.PAYPERHOUR).build());
        // open the gate
        log.info("Parking fee clears, {} is allowed  to exit", payment2GateResDto.getLicensePlate());
        gateIF.openGate(payment2GateResDto.getGateId());
    }

    // adding charge fee on bill
    @Transactional
    public void addCharges(Enforcement2GateResDto enforcement2GateResDto){
        GateSystemInfo gateSystemInfo = gateSystemRepository.findByLicensePlate(enforcement2GateResDto.getLicensePlate());
        String licensePlate  = gateSystemInfo.getLicensePlate();
        if (gateSystemRepository.findByLicensePlate(licensePlate) == null) {
            log.error("LicensePlate {} not found", enforcement2GateResDto.getLicensePlate());
            return;
        }
        gateSystemRepository.updateGateSystemInfo(licensePlate,
                enforcement2GateResDto.getFineReason(), enforcement2GateResDto.getBill());
        log.info("Car {} is charged", gateSystemInfo.getLicensePlate());
    }

    private long generateQRCode() {
        Random random = new Random();
        return Math.abs(random.nextLong()); // change it to always positive
    }

    private long calculateHoursStayed(long entryTimeMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        log.info("entry time {}, exit time {}", entryTimeMillis, currentTimeMillis);
        long diffInMillies = Math.abs(currentTimeMillis - entryTimeMillis);
        return TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    private double getHourlyRate(long entryTimeMillis) {
        // Implement logic to determine hourly rate based on the day
        // For example, different rates for weekdays and weekends
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(entryTimeMillis);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return 500; // Weekend rate
        } else {
            return 300; // Weekday rate
        }
    }

    public Long createVisitorPass(String licensePlate) {
        Long qrCode = generateQRCode();
        GateSystemInfo gateSystemInfo = GateSystemInfo.builder()
                .licensePlate(licensePlate)
                .charge(0)
                .QRCode(qrCode)
                .entryTime(System.currentTimeMillis())
                .isVisitor(true)
                .build();
        gateSystemRepository.save(gateSystemInfo);
        log.info("Visitor pass created for car {}", licensePlate);
        return qrCode;
    }

    private Boolean getSpaceAvailability(String gate) throws JsonProcessingException {
        GateCheckSpaceDto gateCheckSpaceDto = new GateCheckSpaceDto(gate, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonObj = objectMapper.writeValueAsString(gateCheckSpaceDto);
        try {
            return webClient.post()
                    .uri("http://localhost:9083/member-service/availability/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonObj)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .onErrorReturn(false)
                    .block();
        }catch (Exception e){
            log.error("getSpaceAvailability error", e);
            return false;
        }
    }
}
