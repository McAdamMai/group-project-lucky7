package ca.mcmaster.cas735.acme.parking_enforcement.adapters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.FineLicenseDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.FineGateDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.FinePaymentDTO;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AMQPSender {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.custom.messaging.outbound-exchange-management}")
    private String outboundExchangeManagement;

    @Value("${app.custom.messaging.outbound-exchange-payment}")
    private String outboundExchangePayment;

    @Value("${app.custom.messaging.outbound-exchange-gate}")
    private String outboundExchangeGate;

    public void sendManagement(FineLicenseDTO req) {
        rabbitTemplate.convertAndSend(outboundExchangeManagement, "*enforcement2manager",translate(req));
        log.info("send member search to management: {}", req);
    }

    public void sendPayment(FinePaymentDTO req) {
        rabbitTemplate.convertAndSend(outboundExchangePayment, "*enforcement2payment",translate(req));
        log.info("send payment request to the parking_payment service: {}", req);
    }

    public void sendGate(FineGateDTO req) {
        rabbitTemplate.convertAndSend(outboundExchangeGate, "*enforcement2gate",translate(req));
        log.info("send payment request to the gate service: {}", req);
    }

    @Bean
    public TopicExchange outboundManagement() {
        return new TopicExchange(outboundExchangeManagement);
    }

    @Bean
    public TopicExchange outboundPayment() {
        return new TopicExchange(outboundExchangePayment);
    }

    @Bean
    public TopicExchange outboundGate() {
        return new TopicExchange(outboundExchangeGate);
    }

    private <T> String translate(T dto){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsString(dto);
        } catch(IOException e){
            throw new RuntimeException();
        }
    }
}
