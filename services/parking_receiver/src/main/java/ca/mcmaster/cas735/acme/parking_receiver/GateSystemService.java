package ca.mcmaster.cas735.acme.parking_receiver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @Slf4j
public class GateSystemService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Queue names
    private static final String VALIDATION_REQUEST_QUEUE = "validationRequestQueue";
    private static final String VALIDATION_RESPONSE_QUEUE = "validationResponseQueue";

    public void sendValidationRequest(String transponderId) {
        // Create a message with the transponderId and correlation ID
        MessageProperties messageProperties = new MessageProperties();
        Message message = new Message(transponderId.getBytes(), messageProperties);

        // Send the request message to the validationRequestQueue
        rabbitTemplate.convertAndSend(VALIDATION_REQUEST_QUEUE, message);

        System.out.println("Sent validation request for transponder: " + transponderId);

    }

    @RabbitListener(queues = VALIDATION_RESPONSE_QUEUE)
    public void receiveValidationResponse(Message responseMessage) {
        System.out.println("Received validation response message: " + responseMessage);

        if (responseMessage != null) {
            String response = new String(responseMessage.getBody());
            System.out.println("Received validation response: " + response);

            // Handle response (open gate if valid)
            if ("VALID".equals(response)) {
                openGate();
            } else {
                System.out.println("Transponder validation failed.");
            }
        }
    }

    private void openGate() {
        System.out.println("Gate is opening...");
        // Logic to open the gate
    }
}
