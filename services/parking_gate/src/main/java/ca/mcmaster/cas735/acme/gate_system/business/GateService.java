package ca.mcmaster.cas735.acme.gate_system.business;

import ca.mcmaster.cas735.acme.gate_system.adaptors.SenderGateSystem;
import ca.mcmaster.cas735.acme.gate_system.controller.GateController;
import ca.mcmaster.cas735.acme.gate_system.dtos.*;
import ca.mcmaster.cas735.acme.gate_system.model.GateSystemInfo;
import ca.mcmaster.cas735.acme.gate_system.ports.GateIF;
import ca.mcmaster.cas735.acme.gate_system.repository.GateSystemRepository;
import ca.mcmaster.cas735.acme.gate_system.utils.TypeOfClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GateService {
    private final boolean isFull = false;
    private final GateSystemRepository gateSystemRepository;
    private final GateController gateController;
    private final GateIF gateIF;
    private final SenderGateSystem senderGateSystem;

    private static final Set<String> exitGates = new HashSet<>(Arrays.asList("EXIT12345", "EXIT54321", "EXIT67890"));
    private static final Set<String> entryGates = new HashSet<>(Arrays.asList("ENTRY12345", "ENTRY54321", "ENTRY67890"));


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

    public void removeTransponder(String licensePlate) {
        GateSystemInfo gateSystemInfo = gateSystemRepository.findByLicensePlate(licensePlate);
        if (gateSystemInfo == null) {
            log.error("LicensePlate {} not found", licensePlate);
            return;
        }
        gateSystemRepository.delete(gateSystemInfo);
        log.info("Car {} is removed", gateSystemInfo.getLicensePlate());
    }

    public void enterExitParkingLotWithTransponder(String licensePlate, String gate) {
        // This is after being validated
        if (exitGates.contains(gate)) {
            removeTransponder(licensePlate);
            // update availability of parking spots
            senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                    .licensePlate(licensePlate)
                    .exitTime(System.currentTimeMillis())
                    .typeOfClient(TypeOfClient.PAYPERHOUR).build());

        } else if (entryGates.contains(gate)) {
            saveTransponder(ParkingInfoRequest.builder()
                    .licensePlate(licensePlate)
                    .charge(BigDecimal.ZERO)
                    .entryTime(System.currentTimeMillis())
                    .isVisitor(false)
                    .build());
            // update availability of parking spots
            senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                    .licensePlate(licensePlate)
                    .entryTime(System.currentTimeMillis())
                    .typeOfClient(TypeOfClient.PAYPERHOUR).build());
        } else {
            log.error("Invalid gate: {}", gate);
        }
            // Send the transponder
        // update availability of parking spots
        gateIF.openGate(gate);
    }

    public void enterParkingLotWithoutTransponder(String licensePlate, String gate) {
        // generate QR code
        long QRCode = generateQRCode();
        log.info("Generated QR code: {}", QRCode);
        saveTransponder(ParkingInfoRequest.builder()
                .licensePlate(licensePlate)
                .charge(BigDecimal.ZERO)
                .entryTime(System.currentTimeMillis())
                .isVisitor(true)
                .build());
        // update availability of parking spots
        senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                .licensePlate(licensePlate)
                .entryTime(System.currentTimeMillis())
                .typeOfClient(TypeOfClient.PAYPERHOUR).build());
        // send the QR code to mqtt service
        gateIF.generateQRCode(QRCode);
        gateIF.openGate(gate);
    }

    public void createSendPaymentRequest(Long QRCode, String gate) {
        Gate2PaymentReqDto gate2PaymentReqDto = computeParkingPrice(QRCode);
        log.info("Computed parking price: {}", gate2PaymentReqDto.getBill());
        senderGateSystem.sendPaymentRequest(gate2PaymentReqDto);
    }

    public void enterExitParkingLotWithQR(Long QRCode, String gate) {
        if(entryGates.contains(gate)) {
            GateSystemInfo gateSystemInfo = gateSystemRepository.findByQRCode(QRCode);
            if (gateSystemInfo != null && gateSystemInfo.getIsVisitor()) {
                senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                        .licensePlate(gateSystemInfo.getLicensePlate())
                        .entryTime(System.currentTimeMillis())
                        .typeOfClient(TypeOfClient.VISITOR).build());
                gateIF.openGate(gate);
                return;
            }
        }
        if (exitGates.contains(gate)) {
            GateSystemInfo gateSystemInfo = gateSystemRepository.findByQRCode(QRCode);
            if (gateSystemInfo != null && gateSystemInfo.getIsVisitor()) {
                removeTransponder(gateSystemInfo.getLicensePlate());
                senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                        .licensePlate(gateSystemInfo.getLicensePlate())
                        .exitTime(System.currentTimeMillis())
                        .typeOfClient(TypeOfClient.VISITOR).build());
                gateIF.openGate(gate);
                return;
            }
            else{
                createSendPaymentRequest(QRCode, gate);
            }
        }
    }



    public Gate2PaymentReqDto computeParkingPrice(Long QRCode) {
        GateSystemInfo gateSystemInfo = gateSystemRepository.findByQRCode(QRCode);
        if (gateSystemInfo == null) {
            log.error("QRCode {} not found", QRCode);
            return null;
        }

        // Assuming you have a method to get the hourly rate based on the day
        double hourlyRate = getHourlyRate(gateSystemInfo.getEntryTime());
        long hoursStayed = calculateHoursStayed(gateSystemInfo.getEntryTime());

        double totalCharge = hourlyRate * hoursStayed + gateSystemInfo.getCharge().doubleValue();
        // Send the total charge to the payment system
        log.info("Total charge for car {} is {}", gateSystemInfo.getLicensePlate(), totalCharge);

        return Gate2PaymentReqDto.builder()
                .licensePlate(gateSystemInfo.getLicensePlate())
                .bill((int) totalCharge)
                .fineReason(gateSystemInfo.getFineReason())
                .timeStamp((int) System.currentTimeMillis())
                .build();

    }

    public void exitingCar(Payment2GateResDto payment2GateResDto) {
        // update availability of parking spots
        removeTransponder(payment2GateResDto.getLicensePlate());
        senderGateSystem.sendAvailabilities(Gate2AvailabilityResDto.builder()
                .licensePlate(payment2GateResDto.getLicensePlate())
                .exitTime(System.currentTimeMillis())
                .typeOfClient(TypeOfClient.PAYPERHOUR).build());
        // open the gate
        log.info("Parking fee clears, {} is allowed  to exit", payment2GateResDto.getLicensePlate());
        gateIF.openGate(payment2GateResDto.getGate());
    }

    public void addCharges(Enforcement2GateResDto enforcement2GateResDto){
        GateSystemInfo gateSystemInfo = gateSystemRepository.findByLicensePlate(enforcement2GateResDto.getLicensePlate());
        if (gateSystemInfo == null) {
            log.error("LicensePlate {} not found", enforcement2GateResDto.getLicensePlate());
            return;
        }
        gateSystemInfo.setCharge(BigDecimal.valueOf(enforcement2GateResDto.getBill()));
        gateSystemInfo.setFineReason(enforcement2GateResDto.getFineReason());
        gateSystemRepository.updateGateSystemInfo(gateSystemInfo);
        log.info("Car {} is charged", gateSystemInfo.getLicensePlate());
    }

    private long generateQRCode() {
        Random random = new Random();
        return random.nextLong();
    }

    private long calculateHoursStayed(long entryTimeMillis) {
        long currentTimeMillis = System.currentTimeMillis();
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
            return 5.0; // Weekend rate
        } else {
            return 3.0; // Weekday rate
        }
    }


    public Long createVisitorPass(String licensePlate) {
        Long qrCode = generateQRCode();
        GateSystemInfo gateSystemInfo = GateSystemInfo.builder()
                .licensePlate(licensePlate)
                .charge(BigDecimal.ZERO)
                .QRCode(qrCode)
                .entryTime(System.currentTimeMillis())
                .isVisitor(true)
                .build();
        gateSystemRepository.save(gateSystemInfo);
        log.info("Visitor pass created for car {}", licensePlate);
        return qrCode;



    }
}
