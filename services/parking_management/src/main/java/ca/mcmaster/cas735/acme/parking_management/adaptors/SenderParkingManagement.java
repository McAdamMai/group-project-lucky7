package ca.mcmaster.cas735.acme.parking_management.adaptors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.mcmaster.cas735.acme.parking_management.dtos.PaymentReqDto;
import ca.mcmaster.cas735.acme.parking_management.ports.PaymentIF;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderParkingManagement implements PaymentIF{
    private final RabbitTemplate rabbitTemplate;
    @Value("${app.custom.messaging.outbound-exchange-payment}") private String outboundExchangePayment;

    @Override
    public void sendToPayment(PaymentReqDto paymentReqDto) {
        rabbitTemplate.convertAndSend(outboundExchangePayment, "*manager",translate(paymentReqDto));
        log.info("send payment request to the parking_payment service: {}", paymentReqDto);
    }

    @Bean
    public TopicExchange outboundPayment() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangePayment);
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
