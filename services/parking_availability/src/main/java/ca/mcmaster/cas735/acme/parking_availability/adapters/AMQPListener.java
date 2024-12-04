package ca.mcmaster.cas735.acme.parking_availability.adapters;

import ca.mcmaster.cas735.acme.parking_availability.dto.*;
import ca.mcmaster.cas735.acme.parking_availability.business.AvailabilityService;
import ca.mcmaster.cas735.acme.parking_availability.ports.AddSale;
import ca.mcmaster.cas735.acme.parking_availability.repository.SalesRepository;
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
    private final AddSale addSale;
    private final SalesRepository salesRepository;

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
            value = @Queue(value = "gate2avl.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-gate}",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*gate2availability"))
    public void listenGate(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        availabilityService.updateSpace(translate(message, Gate2AvailabilityResDto.class));
    }

    // listening to manager
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "manager_resp.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-manager}",
            ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*manager2availability"))

    public void listenManager(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        salesRepository.updateValidPermits(translate(message, Management2AvailDTO.class).getValid_permits());
    }

    // listening to manager

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment2avl.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-payment}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*payment2availability"))
    public void listenPayment(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        Payment2AvailDTO payment2AvailDTO = translate(message, Payment2AvailDTO.class);
        salesRepository.updatePermitSale(payment2AvailDTO.getPermit_sales());
        salesRepository.updateTotalRevenue(payment2AvailDTO.getTotal_revenue());
        salesRepository.updatePermitRevenue(payment2AvailDTO.getPermit_revenue());
        salesRepository.updateParkingRevenue(payment2AvailDTO.getParking_revenue());
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