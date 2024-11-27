package ca.mcmaster.cas735.acme.gate_system.adaptors;

import ca.mcmaster.cas735.acme.gate_system.dtos.PaymentConfirmation2GateResDto;
import ca.mcmaster.cas735.acme.gate_system.dtos.PaymentID2GateDto;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.amqp.support.AmqpHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.Header;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListenerAMQPSystem {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gate_payment.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-payment_ID}",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*payment2gate"))
    public void listenPaymentID(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message {} from queue: {}", message, queue);
        translate(message, PaymentID2GateDto.class);
        //TODO: add payment ID into model
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gate_payment.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-payment_confirmation}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*payment2gate"))
    public void listenPaymentConfirmation(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message {} from queue: {}", message, queue);
        translate(message, PaymentConfirmation2GateResDto.class);
        //TODO: process the confirmation
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
