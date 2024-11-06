package ca.mcmaster.cas735.acme.parking_receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service @Slf4j
public class PermitManagementService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Set<String> validTransponders = new HashSet<>(Arrays.asList("VALID12345", "VALID54321", "VALID11111", "VALID67890"));

    private static final String VALIDATION_RESPONSE_QUEUE = "validationResponseQueue";

    @RabbitListener(queues = "validationRequestQueue")
    public void handleValidationRequest(Message message) {
        String transponderId = new String(message.getBody());
        String correlationId = message.getMessageProperties().getCorrelationId();

        System.out.println("Received transponder validation request: " + transponderId);

        // Validate the transponder (e.g., check if it exists in the database)
        String validationResult = validateTransponder(transponderId) ? "VALID" : "INVALID";

        // Send the validation result back to the response queue with the same correlation ID
        Message responseMessage = new Message(validationResult.getBytes());
        responseMessage.getMessageProperties().setCorrelationId(correlationId);
        rabbitTemplate.convertAndSend(VALIDATION_RESPONSE_QUEUE, responseMessage);

        System.out.println("Sent validation result: " + validationResult);
    }

    private boolean validateTransponder(String transponderId) {
        TransponderDTO transponder = translate(transponderId);
        return validTransponders.contains(transponder.getN());
    }

    private TransponderDTO translate(String raw) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(raw, TransponderDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
