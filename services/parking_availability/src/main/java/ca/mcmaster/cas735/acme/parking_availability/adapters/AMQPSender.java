package ca.mcmaster.cas735.acme.parking_availability.adapters;

import ca.mcmaster.cas735.acme.parking_availability.dto.*;
import ca.mcmaster.cas735.acme.parking_availability.ports.AddSale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AMQPSender implements AddSale {
    private final RabbitTemplate rabbitTemplate;
    @Value("${app.custom.messaging.outbound-exchange-gate}") private String outboundExchangeGate;
    @Value("${app.custom.messaging.outbound-exchange-monitor}") private String outboundExchangeMonitor;
    @Value("${app.custom.messaging.outbound-exchange-payment}") private String outboundExchangePayment;
    @Value("${app.custom.messaging.outbound-exchange-management}") private String outboundExchangeManagement;

    public void sendToGate(Avl2GateResponseDTO res) {
        rabbitTemplate.convertAndSend(outboundExchangeGate, "*availability2gate",translate(res));
        log.info("send payment request to the parking_payment service: {}", res);
    }
    @Bean
    public TopicExchange outboundGate() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeGate);
    }

    // req to payment
    @Override
    public void request2Payment(InitializationRequest initializationRequest) {
        log.info("sending req to payment {}", initializationRequest);
        rabbitTemplate.convertAndSend(outboundExchangePayment, "*avl2payment",translate(initializationRequest));
    }
    @Bean
    public TopicExchange outboundPayment() {
        return new TopicExchange(outboundExchangePayment);
    }

    // req to management
    @Override
    public void request2Management(InitializationRequest initializationRequest) {
        log.info("sending req to manage {}", initializationRequest);
        rabbitTemplate.convertAndSend(outboundExchangeManagement, "*avl2management",translate(initializationRequest));
    }
    @Bean
    public TopicExchange outboundManagement() {
        return new TopicExchange(outboundExchangeManagement);
    }

    //send to external system
    public void sendToMonitor(InitializationRequest initializationRequest) {
        log.info("send response {} to {}", initializationRequest,outboundExchangeMonitor);
        rabbitTemplate.convertAndSend(outboundExchangeMonitor, "*avl2monitor",translate(initializationRequest));
    }
    @Bean
    public TopicExchange outboundMonitor() {
        return new TopicExchange(outboundExchangeMonitor);
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
