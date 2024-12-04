package ca.mcmaster.cas735.acme.gate_system.adaptors;

import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2AvailabilityResDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PaymentReqDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.Gate2PermitReqDto;
import ca.mcmaster.cas735.acme.gate_system.ports.GateIF;
import ca.mcmaster.cas735.acme.gate_system.utils.GateSystemUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.io.IOException;


@Slf4j
@Service
@RequiredArgsConstructor
public class SenderGateSystem implements GateIF {

    private final RabbitTemplate rabbitTemplate;
    private GateSystemUtils gateSystemUtils;

    @Value("${app.custom.messaging.outbound-exchange-payment}") private String outboundPaymentExchange;
    @Value("${app.custom.messaging.outbound-exchange-management}") private String VALIDATION_TRANSPONDER_REQUEST_QUEUE;
    //private final String VALIDATION_TRANSPONDER_REQUEST_QUEUE = "validationTransponderRequestQueue";
    private final String AVAILABILITY_REQUEST_QUEUE = "availabilityRequestQueue";
    private final String PAYMENT_REQUEST_QUEUE = "paymentRequestQueue";
    private final String QRCODE_REQUEST_QUEUE = "QRCodeRequestQueue";
    private final String OPEN_GATE_QUEUE = "OpenGateQueue";


    @Override
    public void openGate(String gate) {
        log.info("Opening gate: {}", gate);
        rabbitTemplate.convertAndSend(OPEN_GATE_QUEUE, gate);
    }

    @Override
    public void generateQRCode(Long QRCode) {
        log.info("Generating QR code for permit request: {}", QRCode);
        rabbitTemplate.convertAndSend(QRCODE_REQUEST_QUEUE, QRCode);
    }

    @Override
    public void sendValidationRequest(Gate2PermitReqDto gate2PermitReqDto) {
        // Create a message with the transponderId and correlation ID
        //MessageProperties messageProperties = new MessageProperties();
        //Message message = new Message(gateSystemUtils.translateToBytes(gate2PermitReqDto), messageProperties);
        // Send the request message to the validationRequestQueue
        rabbitTemplate.convertAndSend(VALIDATION_TRANSPONDER_REQUEST_QUEUE, "*gate2manager",translate(gate2PermitReqDto));
        log.info("Sent validation request for transponder: {}", gate2PermitReqDto);
    }
    @Bean
    public TopicExchange outboundManagement() {
        // this will create the outbound exchange if it does not exist
        return new TopicExchange(VALIDATION_TRANSPONDER_REQUEST_QUEUE);
    }

    @Override
    public void sendAvailabilities(Gate2AvailabilityResDto gate2AvailabilityResDto) {
        log.info("Sending availabilities: {}", gate2AvailabilityResDto);
        rabbitTemplate.convertAndSend(AVAILABILITY_REQUEST_QUEUE, "*gate2availability" ,translate(gate2AvailabilityResDto));
    }

    @Override
    public void sendPaymentRequest(Gate2PaymentReqDto gate2PaymentReqDto) {
        log.info("Sending payment request for license plate: {}", gate2PaymentReqDto.getLicensePlate());
        rabbitTemplate.convertAndSend(PAYMENT_REQUEST_QUEUE, translate(gate2PaymentReqDto));
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
