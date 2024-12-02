package ca.mcmaster.cas735.acme.parking_availability.adapters;

import ca.mcmaster.cas735.acme.parking_availability.dto.MonitorRequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.RequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.BillDTO;
import ca.mcmaster.cas735.acme.parking_availability.business.AvailabilityService;
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
public class AMQPListener {

    private final AvailabilityService availabilityService;
    
    //listener for monitor
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "client_req.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-topic}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*client2availability"))

    public void listenClient(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        availabilityService.monitor(translate(message, MonitorRequestDTO.class));
    }

    //listen for gate
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gate_req.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-gate}",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*gate2availability"))
    public void listenPayment(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        availabilityService.checkSpace(translate(message, RequestDTO.class));
    }

    // listening to manager
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "manager_req.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-manager}",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*manager2availability"))

    public void listenManager(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        availabilityService.addSale(translate(message, BillDTO.class));
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