package ca.mcmaster.cas735.acme.parking_availability.adapters;

import ca.mcmaster.cas735.acme.parking_availability.dto.MonitorRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.mcmaster.cas735.acme.parking_availability.dto.ResponseDTO;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AMQPSender {
    private final RabbitTemplate rabbitTemplate;
    @Value("${app.custom.messaging.outbound-exchange-gate}") private String outboundExchangeGate;
    @Value("${app.custom.messaging.outbount-exchange-monitor}") private String outboundExchangeMonitor;

    public void sendToGate(ResponseDTO res) {
        rabbitTemplate.convertAndSend(outboundExchangeGate, "*availability2gate",translate(res));
        log.info("send payment request to the parking_payment service: {}", res);
    }

    @Bean
    public TopicExchange outboundPayment() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeGate);
    }

    //send to external system
    public void sendToMonitor(MonitorRespDTO monitorResp) {
        log.info("send response {} to {}", monitorResp,outboundExchangeMonitor);
        rabbitTemplate.convertAndSend(outboundExchangeMonitor, monitorResp);
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
