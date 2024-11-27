package ca.mcmaster.cas735.acme.parking_payment.business;

import ca.mcmaster.cas735.acme.parking_payment.dto.PaymentConfirmation2GateDto;
import ca.mcmaster.cas735.acme.parking_payment.ports.PaymentConfirmation2GateRestIF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentRespUrl implements PaymentConfirmation2GateRestIF {

    private final WebClient webClient = WebClient.builder().baseUrl("http://localhost:9081").build();


    @Override
    public void sendConfirmationToGateREST(PaymentConfirmation2GateDto paymentConfirmation2GateDto){
        log.info("Sending confirmation to gate: {}", paymentConfirmation2GateDto);
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("http://localhost:8081/gate/paymentConfirm")
                        .build(paymentConfirmation2GateDto))
                .bodyValue(paymentConfirmation2GateDto)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
