package ca.mcmaster.cas735.acme.parking_availability.business;

import lombok.extern.slf4j.Slf4j;
import ca.mcmaster.cas735.acme.parking_availability.dto.ResponseDTO;
import ca.mcmaster.cas735.acme.parking_availability.dto.RequestDTO;
import ca.mcmaster.cas735.acme.parking_availability.ports.CheckSpace;
import ca.mcmaster.cas735.acme.parking_availability.ports.Monitor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @Slf4j
public class AvailabilityService implements CheckSpace, Monitor {

    @Override
    public boolean checkSpace(RequestDTO request) {
        if (request.getIsEnter()) {
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void monitor(MonitorRequestDTO request) {
        System.out.println("Display monitor: " + request.getReq());
    }
}
