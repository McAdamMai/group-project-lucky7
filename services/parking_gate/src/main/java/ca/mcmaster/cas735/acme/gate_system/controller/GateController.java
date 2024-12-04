package ca.mcmaster.cas735.acme.gate_system.controller;

import ca.mcmaster.cas735.acme.gate_system.business.GateService;
import ca.mcmaster.cas735.acme.gate_system.ports.GateIF;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/gateSystem")
@RequiredArgsConstructor
public class GateController {

    private final GateService gateService;

    @PostMapping("/visitorPass")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> visitorPass(@RequestBody String licensePlate) {
        try{
            Long visitorPass = gateService.createVisitorPass(licensePlate);
            return ResponseEntity.status(HttpStatus.CREATED).body("Visitor pass created: " + visitorPass);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed: " + e.getMessage());
        }
    }
}
