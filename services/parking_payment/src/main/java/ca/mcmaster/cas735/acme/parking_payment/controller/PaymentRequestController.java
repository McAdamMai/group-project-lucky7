package ca.mcmaster.cas735.acme.parking_payment.controller;

import ca.mcmaster.cas735.acme.parking_payment.dto.Gate2PaymentDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentConfirmation2GateDto;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentConfirmation2GateRestIF;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentRequest2BankIF;
import ca.mcmaster.cas735.acme.parking_payment.utils.TypeOfPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/acme/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestController {

    private final PaymentConfirmation2GateRestIF paymentConfirmation2GateRestIF;
    private final PaymentRequest2BankIF paymentRequest2BankIF;

    @PostMapping("/gate")
    @ResponseStatus(HttpStatus.CREATED)
    public void processPayment(@RequestBody Gate2PaymentDto gate2PaymentDto) {
        PaymentConfirmation2GateDto paymentConfirmation2GateDto = new PaymentConfirmation2GateDto();
        paymentConfirmation2GateDto.setLicensePlate(gate2PaymentDto.getLicensePlate());
        paymentConfirmation2GateDto.setPaymentStatus(TypeOfPaymentStatus.Processing); //initialize message
        paymentRequest2BankIF.sendPaymentRequest(gate2PaymentDto.getLicensePlate(), gate2PaymentDto.getBill()); // send to the bank via message bank
        log.info("visitors pay transponder via bank");
    }
}

