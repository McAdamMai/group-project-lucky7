package ca.mcmaster.cas735.acme.parking_payment.adaptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ca.mcmaster.cas735.acme.parking_payment.dto.EnforcementDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.ManagerDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.ManagerConfirmationDto;
import ca.mcmaster.cas735.acme.parking_payment.dto.GateConfirmationDto;
import ca.mcmaster.cas735.acme.parking_payment.ports.ReqToBankIF;
import ca.mcmaster.cas735.acme.parking_payment.ports.UploadToMacSystemIF;
import ca.mcmaster.cas735.acme.parking_payment.ports.ConfirmationToManager;
import ca.mcmaster.cas735.acme.parking_payment.ports.ConfirmationToGate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentSender implements UploadToMacSystemIF, ReqToBankIF, ConfirmationToManager {

    private final String paymentRequest = "Bank account: , Expire: , Code: ;";
    private final RabbitTemplate rabbitTemplate; //new a rabbit template
    @Value("${app.messaging.outbound-exchange-Mac}") private String outboundExchangeMac; //outbound for mac
    @Value("${app.messaging.outbound-exchange-bank}") private  String outboundExchangeBank; //outbound for bank
    @Value("${app.messaging.inbound-exchange-bank}") private  String inboundExchangeBank;
    @Value("${app.messaging.outbound-exchange-manager}") private  String outboundExchangeManager;
    @Value("${app.messaging.outbound-exchange-gate}") private  String outboundExchangeGate;


    //functions for UploadToMacSystemIF
    @Override
    public void updateFine(EnforcementDto enforcementDto) { // upload fine information to associated users
        log.info("Updating fine status to {}: {}", outboundExchangeMac, enforcementDto);
        rabbitTemplate.convertAndSend(outboundExchangeMac,"*mac", translate(enforcementDto));
    }

    @Override
    public void updateTransponder(ManagerDto managerDto) {
        log.info("Updating transponder status to {}: {}", outboundExchangeMac, managerDto);
        rabbitTemplate.convertAndSend(outboundExchangeMac,"*mac", translate(managerDto));
    }

    @Bean
    public TopicExchange outboundMac() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeMac);
    }

    // functions for ReqToBankIF
    @Override
    public void sendPaymentRequest(String info, Integer bill) {
        String combined = paymentRequest + info + " bill: " + bill;
        log.info("Sending a payment request to bank, amount to be paid is {}", bill);
        rabbitTemplate.convertAndSend(outboundExchangeBank,"*bank", combined);
        Map<String, Object> response = new HashMap<>();
        response.put("info", info);
        response.put("ack", true);
        rabbitTemplate.convertAndSend(inboundExchangeBank, "*bank", translate(response));
        // generate a confirmation for payment service, default value is true
    }

    @Bean
    public TopicExchange outboundBank() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeBank);
    }

    // functions for ConfirmationToManager
    @Override
    public void sendConfirmationToManager(ManagerConfirmationDto managerConfirmationDto){
        log.info("Sending confirmation to manager: {},{}",outboundExchangeManager, managerConfirmationDto);
        rabbitTemplate.convertAndSend(outboundExchangeManager,"*manager", translate(managerConfirmationDto));
    }
    @Bean
    public TopicExchange outboundManager() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeManager);
    }

    // functions for ConfirmationToGate
    /*@Override
    public void sendConfirmationToGate(GateConfirmationDto gateConfirmationDto) {
        log.info("Sending confirmation to gate: {}", gateConfirmationDto);
        rabbitTemplate.convertAndSend(outboundExchangeGate,"*gate", translate(gateConfirmationDto));
    }
    @Bean
    public TopicExchange outboundGate() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeGate);
    }*/

    // universal translator
    private <T> String translate(T dto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsString(dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
