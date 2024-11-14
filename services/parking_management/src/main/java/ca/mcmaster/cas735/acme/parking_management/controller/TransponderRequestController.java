package ca.mcmaster.cas735.acme.parking_management.controller;

import ca.mcmaster.cas735.acme.parking_management.ports.PaymentIF;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.mcmaster.cas735.acme.parking_management.dtos.PaymentReqDto;


@RestController
@RequestMapping("/transponder/")
@RequiredArgsConstructor
public class TransponderRequestController {

    private final PaymentIF paymentIF;

    @PostMapping("/register/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createPaymentRequest(@RequestBody PaymentReqDto paymentReqDto){
        try{
            paymentIF.sendToPayment(paymentReqDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Payment registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed: " + e.getMessage());
        }
    }
}
//post:
// http://localhost:9080/transponder/register/
// body:
// {
//        "macID": "400608194",
//        "bill":10,
//        "licensePlate": "U57U1",
//        "timeStamp": 1731425099,
//        "paymentMethod": "bank"
//}