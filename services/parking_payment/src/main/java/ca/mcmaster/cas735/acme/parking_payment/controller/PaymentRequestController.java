package ca.mcmaster.cas735.acme.parking_payment.controller;

import ca.mcmaster.cas735.acme.parking_payment.dto.GateDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.GateConfirmationDto;
import ca.mcmaster.cas735.acme.parking_payment.ports.ConfirmationToGate;
import ca.mcmaster.cas735.acme.parking_payment.ports.ReqToBankIF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/acme/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestController {

    private final ConfirmationToGate confirmationToGate;
    private final ReqToBankIF reqToBankIF;

    @PostMapping("/gate")
    @ResponseStatus(HttpStatus.CREATED)
    public void processPayment(@RequestBody GateDto gateDto) {
        GateConfirmationDto gateConfirmationDto = new GateConfirmationDto();
        gateConfirmationDto.setLicensePlate(gateDto.getLicensePlate());
        gateConfirmationDto.setPaymentStatus(false); //initialize message
        reqToBankIF.sendPaymentRequest(gateDto.getLicensePlate(), gateDto.getBill()); // send to the bank via message bank
        log.info("visitors pay transponder via bank");
    }
}

