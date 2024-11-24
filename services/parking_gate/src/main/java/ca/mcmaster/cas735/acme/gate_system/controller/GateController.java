package ca.mcmaster.cas735.acme.gate_system.controller;

import ca.mcmaster.cas735.acme.gate_system.business.GateService;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PaymentReqDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PermitReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;

@RestController
@RequestMapping("/gateSystem/")
@RequiredArgsConstructor
public class GateController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GateService gateService;

    //temporarily comment this
    /*@PostMapping("/sendQRCode")
    public String sendPayment(@RequestBody Gate2PaymentReqDto gate2PaymentReqDto) {

        // Send a notification to Notification Service
        // TODO: Change the URL to the correct one

        return restTemplate.postForObject("http://localhost:8081/notify", gate2PaymentReqDto, String.class);

    }*/

    @PostMapping("/sendTransponder")
    public String sendTransponder(@RequestBody BigInteger transponderNumber) {
        // Logic to handle the transponder number

        // Create notification request
        Gate2PermitReqDto request = Gate2PermitReqDto.builder().transponderId(transponderNumber).build();

        // Send a notification to Notification Service
        // TODO: Change the URL to the correct one
        return restTemplate.postForObject("http://localhost:8081/notify", request, String.class);

    }


    @PostMapping("/send")
    public String sendAvailability(@RequestBody BigInteger transponderNumber) {
        // Logic to handle the transponder number

        // Create notification request
        Gate2PermitReqDto request = Gate2PermitReqDto.builder().transponderId(transponderNumber).build();

        // Send a notification to Notification Service
        // TODO: Change the URL to the correct one
        return restTemplate.postForObject("http://localhost:8081/notify", request, String.class);

    }



    // A
}
