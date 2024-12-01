package ca.mcmaster.cas735.acme.parking_availability.controller;

import ca.mcmaster.cas735.acme.parking_availability.dto.ResponseDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.RequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.business.AvailabilityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/check")
    public ResponseDTO checkSpace(@RequestBody RequestDTO request) {
        boolean status = availabilityService.checkSpace(request);
        return new ResponseDTO(status);
    }
}