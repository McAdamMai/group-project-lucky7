package ca.mcmaster.cas735.acme.parking_payment.adaptors;

import ca.mcmaster.cas735.acme.parking_payment.dto.*;
import ca.mcmaster.cas735.acme.parking_payment.ports.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
public class PaymentSender implements Payment2MacSystemIF, PaymentRequest2BankIF,
        PaymentConfirmation2ManagementIF, PaymentConfirmation2GateMsgBusIF, Payment2Avl {

    private final String paymentRequest = "Bank account: test_account, PaymentId: ";
    private final RabbitTemplate rabbitTemplate; //new a rabbit template
    @Value("${app.messaging.outbound-exchange-Mac}") private String outboundExchangeMac; //outbound for mac
    @Value("${app.messaging.outbound-exchange-bank}") private  String outboundExchangeBank; //outbound for bank
    @Value("${app.messaging.inbound-exchange-bank}") private  String inboundExchangeBank;
    @Value("${app.messaging.outbound-exchange-manager}") private  String outboundExchangeManager;
    @Value("${app.messaging.outbound-exchange-gate}") private  String outboundExchangeGate;
    @Value("${app.messaging.outbound-exchange-pos}") private String outboundExchangePos;
    @Value("${app.messaging.outbound-exchange-avl}") private  String outboundExchangeAvl;

    //functions for UploadToMacSystemIF
    @Override
    public void updateFine(Enforcement2PaymentDto enforcement2PaymentDto) { // upload fine information to associated users
        log.info("Updating fine status to {}: {}", outboundExchangeMac, enforcement2PaymentDto);
        rabbitTemplate.convertAndSend(outboundExchangeMac,"*mac", translate(enforcement2PaymentDto));
    }

    @Override
    public void updateTransponder(PaymentConfirmation2ManagementDto paymentConfirmation2ManagementDto) {
        log.info("Updating transponder status to {}: {}", outboundExchangeMac, paymentConfirmation2ManagementDto);
        rabbitTemplate.convertAndSend(outboundExchangeMac,"*mac", translate(paymentConfirmation2ManagementDto));
    }

    @Bean
    public TopicExchange outboundMac() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeMac);
    }

    // functions for ReqToBankIF
    @Override
    public void sendPaymentRequest(String paymentId, Integer bill) {
        String msg = paymentRequest + paymentId + "Bill:" + bill;
        log.info("Sending a payment request to bank, amount to be paid is {}", bill);
        rabbitTemplate.convertAndSend(outboundExchangeBank,"*payment2bank", msg);
        //
        Map<String, Object> response = new HashMap<>();
        response.put("paymentID",paymentId);
        response.put("ack", true);
        rabbitTemplate.convertAndSend(inboundExchangeBank, "*bank2payment", translate(response));
        // generate a confirmation for payment service, default value is true
    }

    @Bean
    public TopicExchange outboundBank() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeBank);
    }

    // functions for ConfirmationToManager
    @Override
    public void sendConfirmationToManager(PaymentConfirmation2ManagementDto paymentConfirmation2ManagementDto){
        log.info("Sending confirmation to manager: {},{}",outboundExchangeManager, paymentConfirmation2ManagementDto);
        rabbitTemplate.convertAndSend(outboundExchangeManager,"*payment2manager", translate(paymentConfirmation2ManagementDto));
    }
    @Bean
    public TopicExchange outboundManager() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeManager);
    }

    // functions for ConfirmationToGate
    @Override
    public void sendConfirmationToGate(PaymentConfirmation2GateDto paymentConfirmation2GateDto) {
        log.info("Sending confirmation to gate: {}", paymentConfirmation2GateDto);
        rabbitTemplate.convertAndSend(outboundExchangeGate,"*payment2gate", translate(paymentConfirmation2GateDto));
    }

    @Bean
    public TopicExchange outboundGateConfirmation() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(outboundExchangeGate);
    }

    // functions for associating payment ID with POS, this function is to external
    @Override
    public void sendPaymentIDPos(PaymentID2PosDto paymentID2PosDto){
        log.info("Sending confirmation to gate: {}", paymentID2PosDto);
        rabbitTemplate.convertAndSend(outboundExchangePos,"*payment2pos", translate(paymentID2PosDto));
    }

    @Bean
    public TopicExchange outboundGateID() {
        return new TopicExchange(outboundExchangePos);
    }

    @Override
    public void send2avl(Payment2AvailDTO payment2AvlDto) {
        log.info("Sending a payment to avl: {}", payment2AvlDto);
        rabbitTemplate.convertAndSend(outboundExchangeAvl,"*payment2availability", translate(payment2AvlDto));
    }
    @Bean
    public TopicExchange outboundAvl() {
        return new TopicExchange(outboundExchangeAvl);
    }

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
