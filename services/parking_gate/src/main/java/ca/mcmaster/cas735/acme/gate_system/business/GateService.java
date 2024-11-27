package ca.mcmaster.cas735.acme.gate_system.business;

import ca.mcmaster.cas735.acme.gate_system.controller.GateController;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PaymentReqDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PermitReqDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.ParkingInfoRequest;
import ca.mcmaster.cas735.acme.gate_system.model.GateSystemInfo;
import ca.mcmaster.cas735.acme.gate_system.ports.GateIF;
import ca.mcmaster.cas735.acme.gate_system.ports.PaymentLaunchingIF;
import ca.mcmaster.cas735.acme.gate_system.repository.GateSystemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GateService {
    // compute the parking price
    // open the gate
    private final GateSystemRepository gateSystemRepository;
    private final GateController gateController;
    private final GateIF gateIF;
    private final PaymentLaunchingIF paymentLaunchingIF;
    private final WebClient webClient = WebClient.builder().baseUrl("http://localhost:9081").build();

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

    public void enterExitParkingLotWithTransponder(BigInteger transponderNumber, boolean isExit) {
        // response should return license plate
        String licensePlate = gateController.sendTransponder(transponderNumber);

        log.info("Transponder response: {}", licensePlate);
        if (licensePlate == null) {
            log.error("Failed to scan transponder. Please try again.");
            return;
        }
        log.error("Transponder scanned successfully. Gate is opened.");
        if (!isExit) {
            saveTransponder(ParkingInfoRequest.builder()
                    .licensePlate(licensePlate)
                    .charge(BigDecimal.ZERO)
                    .entryTime(System.currentTimeMillis())
                    .isVisitor(false)
                    .build());
            // update availability of parking spots
        }
        else{
            removeTransponder(licensePlate);
            // update availability of parking spots
        }
            // Send the transponder
        // update availability of parking spots
        openGate();
    }

    public void enterParkingLotWithoutTransponder(String licensePlate) {
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
        // send the QR code to mqtt service
        openGate();


    }

    public void exitParkingLotWithQR(Long QRCode) {

        Gate2PaymentReqDto gate2PaymentReqDto = computeParkingPrice(QRCode);
        log.info("Computed parking price: {}", gate2PaymentReqDto.getBill());
        //sendPayment(gate2PaymentReqDto); replace this with launchPayment
        // Send the parking price to the payment system
        paymentLaunchingIF.launchPaymentMsgBus(gate2PaymentReqDto); // Send payment info to payment system
        log.info("Sending parking price to payment system: {}", gate2PaymentReqDto);
        // update availability of parking spots
        removeTransponder(gate2PaymentReqDto.getLicensePlate());// shouldn't the gate stay close until it have received msg from gate?
        // open the gate
        openGate();
    }


    //comment: if this method only used in GateService, should it better a private method
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

    public void openGate() {
        // Open the gate
        log.info("Gate is opened");
        gateIF.openGate();
    }

    /*public void updateAvailabilityOfParkingSpots(boolean isExit) {
        // Update the availability of parking spots
        log.info("Updating availability of parking spots");
        gateIF.updateAvailabilityOfParkingSpots(isExit);

    }*/
    private void sendPaymentREST(Gate2PaymentReqDto gate2PaymentReqDto){
        log.info("Sending payment request to payment: {}", gate2PaymentReqDto);
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("http://localhost:9081/acme/payment/gate")
                        .build(gate2PaymentReqDto))
                .bodyValue(gate2PaymentReqDto)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
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


}
