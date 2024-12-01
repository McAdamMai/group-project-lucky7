package ca.mcmaster.cas735.acme.parking_availability;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Define queues
    public static final String FINE_PAYMENT_QUEUE = "finePaymentQueue";
    public static final String FINE_GATE_QUEUE = "fineGateQueue";

    @Bean
    public Queue finePaymentQueue() {
        return new Queue(FINE_PAYMENT_QUEUE, false);
    }

    @Bean
    public Queue fineGateQueue() {
        return new Queue(FINE_GATE_QUEUE, false);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("enforcement_msg");
    }

    @Bean
    public Binding bindingValidationRequest(Queue finePaymentQueue, DirectExchange exchange) {
        return BindingBuilder.bind(finePaymentQueue).to(exchange).with("finePaymentKey");
    }

    @Bean
    public Binding bindingValidationResponse(Queue fineGateQueue, DirectExchange exchange) {
        return BindingBuilder.bind(fineGateQueue).to(exchange).with("fineGateKey");
    }
}
