package ca.mcmaster.cas735.acme.parking_enforcement.adapters;

import ca.mcmaster.cas735.acme.parking_enforcement.dto.MemberDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.FineLicenseDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.business.EnforcementSystemService;
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

    private final EnforcementSystemService enforcementService;

//    public AMPQListener(EnforcementSystemService enforcementService) {
//        this.enforcementService = enforcementService;
//    }

    //listener for client
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "client_req.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-client}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*client2enforcement"))

    public void listenClient(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        enforcementService.findMember(translate(message, FineLicenseDTO.class));
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "management_res.queue", durable = "true"),
            exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-management}",
                    ignoreDeclarationExceptions = "true", type = "topic"),
            key = "*manager2enforcement"))

    public void listenManagement(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
        log.info("receive message from {}, {}", queue, message);
        enforcementService.sendFine(translate(message, MemberDTO.class));
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