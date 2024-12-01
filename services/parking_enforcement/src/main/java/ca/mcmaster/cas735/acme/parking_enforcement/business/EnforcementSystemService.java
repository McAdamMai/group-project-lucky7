package ca.mcmaster.cas735.acme.parking_enforcement.business;

import ca.mcmaster.cas735.acme.parking_enforcement.adapters.SearchREST;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.*;
import lombok.extern.slf4j.Slf4j;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.SearchRequestDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.ports.FineFilter;
import ca.mcmaster.cas735.acme.parking_enforcement.ports.FinePayment;
import ca.mcmaster.cas735.acme.parking_enforcement.ports.FineGate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @Slf4j
public class EnforcementSystemService implements FineFilter, FineGate, FinePayment {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    private final SearchREST searchRest;

    // Queue names
    private static final String FINE_PAYMENT_QUEUE = "finePaymentQueue";
    private static final String FINE_GATE_QUEUE = "fineGateQueue";

    @Autowired
    public EnforcementSystemService(SearchREST searchRest) {
        this.searchRest = searchRest;
    }

    @Override
    public void sendFine(String message) {
        String license = new String(message.getBody());
        FineLicenseDTO fineLicense = translateLicense(license);
        SearchRequestDTO request = new SearchRequestDTO();
        request.setLicense(fineLicense.getLicense());
        MemberDTO member = searchRest.lookupByMemberId(request);
        if (member.getFound()) {
            FinePaymentDTO fineP = new FinePaymentDTO();
            fineP.setMacID(member.getMacID());
            fineP.setLicensePlate(fineLicense.getLicense());
            fineP.setBill(15);
            fineP.setTimeStamp(fineLicense.getTimeStamp());
            fineP.setFineReason(fineLicense.getReason());
            sendFinePayment(fineP);
        } else {
            FineGateDTO fineG = new FineGateDTO();
            fineG.setLicense(fineLicense.getLicense());
            fineG.setBill(15);
            sendFineGate(fineG);
        }
    }

    @Override
    public void sendFinePayment(FinePaymentDTO fine) {
        // Send the request message to the validationRequestQueue
        rabbitTemplate.convertAndSend(FINE_PAYMENT_QUEUE, fine);

        System.out.println("Sent fine to payment for license: " + fine.getLicense());
    }

    @Override
    public void sendFineGate(FineGateDTO fine) {
        // Send the request message to the validationRequestQueue
        rabbitTemplate.convertAndSend(FINE_GATE_QUEUE, fine);

        System.out.println("Sent fine to gate for license: " + fine.getLicense());
    }

    private FineLicenseDTO translateLicense(String raw) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(raw, FineLicenseDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
