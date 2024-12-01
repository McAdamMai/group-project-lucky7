package ca.mcmaster.cas735.acme.parking_availability.adapters;

import ca.mcmaster.cas735.acme.parking_availability.business.AvailabilityService;
import ca.mcmaster.cas735.acme.parking_availability.dto.MonitorRequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.ports.Monitor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityAMQPListener {

    private final Monitor monitor;

        //listener for payment
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "payment_req.queue", durable = "true"),
                exchange = @Exchange(value = "${app.custom.messaging.inbound-exchange-topic}",
                        ignoreDeclarationExceptions = "true", type = "topic"),
                key = "*manager"))
        public void listenGate(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue){
            log.info("receive message from {}, {}", queue, message);
            monitor.send2monitor(translate(message, MonitorRequestDTO.class));
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

