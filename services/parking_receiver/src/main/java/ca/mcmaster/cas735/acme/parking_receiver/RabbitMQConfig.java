package ca.mcmaster.cas735.acme.parking_receiver;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Define queues
    public static final String VALIDATION_REQUEST_QUEUE = "validationRequestQueue";
    public static final String VALIDATION_RESPONSE_QUEUE = "validationResponseQueue";

    @Bean
    public Queue validationRequestQueue() {
        return new Queue(VALIDATION_REQUEST_QUEUE, false);
    }

    @Bean
    public Queue validationResponseQueue() {
        return new Queue(VALIDATION_RESPONSE_QUEUE, false);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("parkingExchange");
    }

    @Bean
    public Binding bindingValidationRequest(Queue validationRequestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(validationRequestQueue).to(exchange).with("validationRequestKey");
    }

    @Bean
    public Binding bindingValidationResponse(Queue validationResponseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(validationResponseQueue).to(exchange).with("validationResponseKey");
    }
}
