    package ca.mcmaster.cas735.acme.parking_availability.controller;

    import ca.mcmaster.cas735.acme.parking_availability.dto.GateCheckSpaceDto;
    import ca.mcmaster.cas735.acme.parking_availability.ports.GateReq;
    import ca.mcmaster.cas735.acme.parking_availability.repository.LotRepository;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.Objects;

    @Slf4j
    @RestController
    @RequestMapping("/availability")
    @RequiredArgsConstructor
    public class AvailabilityController {

        private final GateReq gateReq;

        @PostMapping("/check")
        @ResponseStatus(HttpStatus.OK)
        public ResponseEntity<Boolean> responseSpaceAvailability(@RequestBody GateCheckSpaceDto gateCheckSpaceDto) {
            try{
                log.info("get message for checking space {}", gateCheckSpaceDto);
                return ResponseEntity.status(HttpStatus.OK).body(gateReq.checkSpace(gateCheckSpaceDto));
            }catch (Exception e){
                log.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
            }
        }
    }
