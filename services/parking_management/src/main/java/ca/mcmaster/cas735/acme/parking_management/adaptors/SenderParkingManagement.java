package ca.mcmaster.cas735.acme.parking_management.adaptors;

import ca.mcmaster.cas735.acme.parking_management.dtos.MemberDTO;
import ca.mcmaster.cas735.acme.parking_management.dtos.AvailabilityResp;
import ca.mcmaster.cas735.acme.parking_management.dtos.Management2MacDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.Permit2GateResDto;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2GateIF;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2MacSystemIF;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2avlIF;
import ca.mcmaster.cas735.acme.parking_management.ports.Management2EnforcementIF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ca.mcmaster.cas735.acme.parking_management.dtos.Management2PaymentDto;
import ca.mcmaster.cas735.acme.parking_management.ports.PaymentIF;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderParkingManagement implements PaymentIF, Management2MacSystemIF, Management2GateIF, Management2avlIF, Management2EnforcementIF {
    private final RabbitTemplate rabbitTemplate;
    @Value("${app.custom.messaging.outbound-exchange-payment}") private String outboundExchangePayment;
    @Value("${app.custom.messaging.outbound-exchange-Mac}") private String outboundExchangeMac;
    @Value("${app.custom.messaging.outbound-exchange-gate}") private String outboundExchangeGate;
    @Value("${app.custom.messaging.outbound-exchange-availability}") private String outboundExchangeAvailability;
    @Value("${app.custom.messaging.outbound-exchange-enforcement}") private String outboundExchangeEnforcement;

    @Override
    public void sendToPayment(Management2PaymentDto management2PaymentDto) {
        rabbitTemplate.convertAndSend(outboundExchangePayment, "*manager2payment",translate(management2PaymentDto));
        log.info("send payment request to the parking_payment service: {}", management2PaymentDto);
    }

    @Bean
    public TopicExchange outboundPayment() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangePayment);
    }

    @Override
    public void updateTransponder(Management2MacDto management2MacDto){
        rabbitTemplate.convertAndSend(outboundExchangeMac, "*manager2mac",translate(management2MacDto));
        log.info("update transponder to the external mac system: {}", management2MacDto);
    }
    @Bean
    public TopicExchange outboundMac() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeMac);
    }

    @Override
    public void update2gate(Permit2GateResDto permit2GateResDto){
        rabbitTemplate.convertAndSend(outboundExchangeGate, "*manager2gate", translate(permit2GateResDto));
        log.info("update permit to the gate: {} to {}", permit2GateResDto, outboundExchangeGate);
    }
    @Bean
    public TopicExchange outboundGate() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeGate);
    }

    @Override
    public void send2val(AvailabilityResp availabilityResp) {
        log.info("send2val to the parking_payment service: {}", availabilityResp);
        rabbitTemplate.convertAndSend(outboundExchangeAvailability, "*manager2availability", translate(availabilityResp));
    }
    @Bean
    public TopicExchange outboundAvl() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeAvailability);
    }

    @Override
    public void update2enforcement(MemberDTO member){
        rabbitTemplate.convertAndSend(outboundExchangeEnforcement, "*manager2enforcement", translate(member));
        log.info("update permit to the gate: {} to {}", member, outboundExchangeEnforcement);
    }
    @Bean
    public TopicExchange outboundEnforcement() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeEnforcement);
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
