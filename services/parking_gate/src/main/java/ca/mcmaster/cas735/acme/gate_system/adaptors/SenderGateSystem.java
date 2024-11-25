package ca.mcmaster.cas735.acme.gate_system.adaptors;

import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PaymentReqDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PermitReqDto;
import ca.mcmaster.cas735.acme.gate_system.ports.GateIF;
import ca.mcmaster.cas735.acme.gate_system.ports.PaymentLaunchingIF;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@Service
@RequiredArgsConstructor
public class SenderGateSystem implements GateIF, PaymentLaunchingIF {

    private final RabbitTemplate rabbitTemplate;
    @Value("${app.custom.messaging.outbound-exchange-payment}") private String outboundGate;
    @Value("${app.custom.messaging.outbound-exchange-payment}") private String outboundPayment;


    @Override
    public void openGate() {
        log.info("Opening gate");
        rabbitTemplate.convertAndSend(outboundGate, "gate");
    }

    @Override
    public void launchPaymentMsgBus(Gate2PaymentReqDto gate2PaymentReqDto){
        log.info("launching payment to {}, content: {}", outboundPayment, gate2PaymentReqDto);
        rabbitTemplate.convertAndSend(outboundPayment, "*gate2payment", translate(gate2PaymentReqDto));
    }

    private <T> String translate(T dto){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsString(dto);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
