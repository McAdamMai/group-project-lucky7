package ca.mcmaster.cas735.acme.parking_payment.business;

import ca.mcmaster.cas735.acme.parking_payment.dto.GateConfirmationDto;
import ca.mcmaster.cas735.acme.parking_payment.ports.ConfirmationToGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentRespUrl implements ConfirmationToGate {

    private final WebClient webClient = WebClient.builder().baseUrl("http://localhost:9081").build();


    @Override
    public void sendConfirmationToGate(GateConfirmationDto gateConfirmationDto){
        log.info("Sending confirmation to gate: {}", gateConfirmationDto);
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("http://localhost:8081/gate/paymentConfirm")
                        .build(gateConfirmationDto))
                .bodyValue(gateConfirmationDto)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
