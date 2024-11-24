package ca.mcmaster.cas735.acme.gate_system.adaptors;

import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PermitReqDto;
import ca.mcmaster.cas735.acme.gate_system.ports.GateIF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class SenderGateSystem implements GateIF {

    private final RabbitTemplate rabbitTemplate;
    @Value("${app.custom.messaging.outbound-exchange-payment}") private String outboundGate;


    @Override
    public void openGate() {
        log.info("Opening gate");
        rabbitTemplate.convertAndSend(outboundGate, "gate");
    }

}
