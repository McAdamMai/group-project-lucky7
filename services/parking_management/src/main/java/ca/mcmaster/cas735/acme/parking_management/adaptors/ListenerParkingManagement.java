package ca.mcmaster.cas735.acme.parking_management.adaptors;

import ca.mcmaster.cas735.acme.parking_management.business.OrderProcessorIF;
import ca.mcmaster.cas735.acme.parking_management.dtos.Gate2PermitReqDto;
import ca.mcmaster.cas735.acme.parking_management.dtos.Payment2ManagementDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListenerParkingManagement {

    private final OrderProcessorIF orderProcessor;

    //listener for payment
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment_req.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-payment}",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*payment2manager"))
    public void listenPayment(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        orderProcessor.processPayment(translate(message, Payment2ManagementDto.class));
         }

         //listener for the gate
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gate_req.queue", durable = "true"),
            exchange = @Exchange(value = "gate2manager_msg",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*gate2manager"
        ))
    public void listenGate(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        orderProcessor.processGateRequest(translate(message, Gate2PermitReqDto.class));
    }
    //listener for others
    //...
    //...
    //...
    // universal functions
    private <T> T byteTranslate(byte[] message, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(message, clazz);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private <T> T translate(String message, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(message, clazz);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
